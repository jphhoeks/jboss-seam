package org.jboss.seam.web;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.jboss.seam.util.Resources;
import org.jboss.seam.web.fileupload.ParameterParser;

/**
 * Request wrapper for supporting multipart requests, used for file uploading.
 * 
 * @author Shane Bryzak
 */
public class MultipartRequestImpl extends HttpServletRequestWrapper implements MultipartRequest {
	private static final String PARAM_NAME = "name";
	private static final String PARAM_FILENAME = "filename";
	private static final String PARAM_CONTENT_TYPE = "Content-Type";

	private static final int BUFFER_SIZE = 2048;
	private static final int MAX_BUFFER_SIZE = 16_384;
	private static final int CHUNK_SIZE = 512;

	private boolean createTempFiles;

	private String encoding = null;

	private Map<String, Param> parameters = null;
	
	private HttpServletRequest request;

	private enum ReadState {
		BOUNDARY, HEADERS, DATA
	}

	private static final byte CR = 0x0d;
	private static final byte LF = 0x0a;
	private static final byte[] CR_LF = { CR, LF };

	private abstract class Param {
		private String name;

		public Param(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public abstract void appendData(byte[] data, int start, int length) throws IOException;
	}

	private class ValueParam extends Param {
		private Object value = null;
		private ByteArrayOutputStream buf = new ByteArrayOutputStream();

		public ValueParam(String name) {
			super(name);
		}

		@Override
		public void appendData(byte[] data, int start, int length) throws IOException {
			buf.write(data, start, length);
		}

		public void complete() throws UnsupportedEncodingException {
			String val = encoding == null ? new String(buf.toByteArray()) : new String(buf.toByteArray(), encoding);
			if (value == null) {
				value = val;
			} else {
				if (!(value instanceof List)) {
					List<String> v = new ArrayList<String>();
					v.add((String) value);
					value = v;
				}

				((List) value).add(val);
			}
			buf.reset();
		}

		public Object getValue() {
			return value;
		}
	}

	private class FileParam extends Param {
		private String filename;
		private String contentType;
		private int fileSize;

		private ByteArrayOutputStream bOut = null;
		private OutputStream fOut = null;
		private File tempFile = null;

		public FileParam(String name) {
			super(name);
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public int getFileSize() {
			return fileSize;
		}

		public void createTempFile() {
			try {
				tempFile = File.createTempFile(new UID().toString().replace(':', '-'), ".upload");
				tempFile.deleteOnExit();
				fOut = new BufferedOutputStream(
						Files.newOutputStream(tempFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
			} catch (IOException ex) {
				throw new FileUploadException("Could not create temporary file", ex);
			}
		}

		@Override
		public void appendData(byte[] data, int start, int length) throws IOException {
			if (fOut != null) {
				fOut.write(data, start, length);
				fOut.flush();
			} else {
				if (bOut == null) {
					bOut = new ByteArrayOutputStream();
				}
				bOut.write(data, start, length);
			}

			fileSize += length;
		}

		public byte[] getData() {
			if (fOut != null) {
				Resources.close(fOut);
				fOut = null;
			}

			if (bOut != null) {
				return bOut.toByteArray();
			} else if (tempFile != null && tempFile.exists()) {

				try {
					byte[] data = Files.readAllBytes(tempFile.toPath());
					tempFile.delete();
					return data;
				} catch (IOException ignored) {
					/* too bad? */
				}

			}

			return null;
		}

		public InputStream getInputStream() {
			if (fOut != null) {
				Resources.close(fOut);
				fOut = null;
			}

			if (bOut != null) {
				return new ByteArrayInputStream(bOut.toByteArray());
			} else if (tempFile != null) {
				try {
					// FIXME try to not rely on FileInputStream
					return new FileInputStream(tempFile) {
						@Override
						public void close() throws IOException {
							super.close();
							tempFile.delete();
						}
					};
				} catch (FileNotFoundException ignored) {
					//
				}
			}

			return null;
		}
	}



	public MultipartRequestImpl(HttpServletRequest request, boolean createTempFiles, int maxRequestSize) {
		super(request);
		this.request = request;
		this.createTempFiles = createTempFiles;

		String contentLength = request.getHeader("Content-Length");
		if (contentLength != null && maxRequestSize > 0 && Integer.parseInt(contentLength) > maxRequestSize) {
			throw new FileUploadException("Multipart request is larger than allowed size");
		}
	}

	private void parseRequest() {
		byte[] boundaryMarker = getBoundaryMarker(request.getContentType());
		if (boundaryMarker == null) {
			throw new FileUploadException("The request was rejected because " + "no multipart boundary was found");
		}

		encoding = request.getCharacterEncoding();

		parameters = new HashMap<String, Param>();
		InputStream input = null;
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			Map<String, String> headers = new HashMap<String, String>();

			ReadState readState = ReadState.BOUNDARY;

			input = request.getInputStream();
			int read = input.read(buffer);
			int pos = 0;

			Param p = null;

			// This is a fail-safe to prevent infinite loops from occurring in some environments
			int loopCounter = 20;

			while (read > 0 && loopCounter > 0) {
				for (int i = 0; i < read; i++) {
					switch (readState) {
					case BOUNDARY: {
						if (checkSequence(buffer, i, boundaryMarker) && checkSequence(buffer, i + 2, CR_LF)) {
							readState = ReadState.HEADERS;
							i += 2;
							pos = i + 1;
						}
						break;
					}
					case HEADERS: {
						if (checkSequence(buffer, i, CR_LF)) {
							// Check next CR_LF available in the buffer
							if (read <= i + CR_LF.length) { /* if there is no space left for CRLF in the buffer, 
															force pushing of remaining (unread) bytes to the beginning of the buffer,
															read more bytes and try again */
								i = read;
								break;
							}
							String param = (encoding == null) ? new String(buffer, pos, i - pos - 1)
									: new String(buffer, pos, i - pos - 1, encoding);
							parseParams(param, ";", headers);

							if (checkSequence(buffer, i + CR_LF.length, CR_LF)) {
								readState = ReadState.DATA;
								i += CR_LF.length;
								pos = i + 1;

								String paramName = headers.get(PARAM_NAME);
								if (paramName != null) {
									if (headers.containsKey(PARAM_FILENAME)) {
										FileParam fp = new FileParam(paramName);
										if (createTempFiles) {
											fp.createTempFile();
										}
										fp.setContentType(headers.get(PARAM_CONTENT_TYPE));
										fp.setFilename(headers.get(PARAM_FILENAME));
										p = fp;
									} else {
										p = parameters.get(paramName);
										if (p == null) {
											p = new ValueParam(paramName);
										}
									}

									if (!parameters.containsKey(paramName)) {
										parameters.put(paramName, p);
									}
								}

								headers.clear();
							} else {
								pos = i + 1;
							}
						}
						break;
					}
					case DATA: {
						// If we've encountered another boundary...
						if (checkSequence(buffer, i - boundaryMarker.length - CR_LF.length, CR_LF)
								&& checkSequence(buffer, i, boundaryMarker)) {
							// Write any data before the boundary (that hasn't already been written) to the param
							if (pos < i - boundaryMarker.length - CR_LF.length - 1) {
								p.appendData(buffer, pos, i - pos - boundaryMarker.length - CR_LF.length - 1);
							}

							if (p instanceof ValueParam) {
								((ValueParam) p).complete();
							}

							if (checkSequence(buffer, i + CR_LF.length, CR_LF)) {
								i += CR_LF.length;
								pos = i + 1;
							} else {
								pos = i;
							}

							readState = ReadState.HEADERS;
						}
						// Otherwise write whatever data we have to the param
						else if (i > (pos + boundaryMarker.length + CHUNK_SIZE + CR_LF.length)) {
							p.appendData(buffer, pos, CHUNK_SIZE);
							pos += CHUNK_SIZE;
						}
						break;
					}
					}
				}

				if (pos < read) {
					// move the bytes that weren't read to the start of the buffer
					int bytesNotRead = read - pos;

					if (buffer.length == bytesNotRead && buffer.length < MAX_BUFFER_SIZE) {

						// if no end of parameter value can be found in the
						// buffer, we have to increase size of the buffer
						byte[] buffer1 = new byte[buffer.length * 2];
						System.arraycopy(buffer, 0, buffer1, 0, buffer.length);
						buffer = buffer1;

						read = input.read(buffer, bytesNotRead, buffer.length - bytesNotRead);
						if (read == -1) {
							// too bad - nothing more to read (EOF) and last parameter value couldn't be parsed
							break;
						}
						read += bytesNotRead;
					} else {
						System.arraycopy(buffer, pos, buffer, 0, bytesNotRead);
						read = input.read(buffer, bytesNotRead, buffer.length - bytesNotRead);

						if (read == -1) {
							// too bad - nothing more to read (EOF) and last parameter value couldn't be parsed
							break;
						}

						if (read == 0) {
							loopCounter--;
						}

						read += bytesNotRead;
					}
				} else {
					read = input.read(buffer);
				}

				pos = 0;
			}
		} catch (IOException ex) {
			throw new FileUploadException("IO Error parsing multipart request", ex);
		}
		finally {
			Resources.close(input);
		}
	}

	private byte[] getBoundaryMarker(String contentType) {
		Map<String, String> params = parseParams(contentType, ";");
		String boundaryStr = params.get("boundary");

		if (boundaryStr == null) {
			return null;
		}
		return boundaryStr.getBytes(StandardCharsets.ISO_8859_1);
	}

	/**
	* Checks if a specified sequence of bytes ends at a specific position
	* within a byte array.
	* 
	* @param data
	* @param pos
	* @param seq
	* @return boolean indicating if the sequence was found at the specified position
	*/
	private boolean checkSequence(byte[] data, int pos, byte[] seq) {
		if (pos - seq.length < -1 || pos >= data.length) {
			return false;
		}

		for (int i = 0; i < seq.length; i++) {
			if (data[(pos - seq.length) + i + 1] != seq[i]) {
				return false;
			}
		}

		return true;
	}

	public Map<String, String> parseParams(String paramStr, String separator) {
		Map<String, String> paramMap = new HashMap<String, String>();
		parseParams(paramStr, separator, paramMap);
		return paramMap;
	}

	private void parseParams(String paramStr, String separator, Map<String, String> paramMap) {

		ParameterParser parser = new ParameterParser();
		Map<String, String> params = parser.parse(paramStr, separator.toCharArray());
		paramMap.putAll(params);
	}

	private Param getParam(String name) {
		if (parameters == null) {
			parseRequest();
		}
		return parameters.get(name);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		if (parameters == null) {
			parseRequest();
		}

		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public byte[] getFileBytes(String name) {
		Param p = getParam(name);
		return (p instanceof FileParam) ? ((FileParam) p).getData() : null;
	}

	@Override
	public InputStream getFileInputStream(String name) {
		Param p = getParam(name);
		return (p instanceof FileParam) ? ((FileParam) p).getInputStream() : null;
	}

	@Override
	public String getFileContentType(String name) {
		Param p = getParam(name);
		return (p instanceof FileParam) ? ((FileParam) p).getContentType() : null;
	}

	@Override
	public String getFileName(String name) {
		Param p = getParam(name);
		return (p instanceof FileParam) ? ((FileParam) p).getFilename() : null;
	}

	@Override
	public int getFileSize(String name) {
		Param p = getParam(name);
		return (p instanceof FileParam) ? ((FileParam) p).getFileSize() : -1;
	}

	@Override
	public String getParameter(String name) {
		Param p = getParam(name);
		if (p instanceof ValueParam) {
			ValueParam vp = (ValueParam) p;
			if (vp.getValue() instanceof String) {
				return (String) vp.getValue();
			}
		} else if (p instanceof FileParam) {
			return "---BINARY DATA---";
		} else {
			return super.getParameter(name);
		}

		return null;
	}

	@Override
	public String[] getParameterValues(String name) {
		Param p = getParam(name);
		if (p instanceof ValueParam) {
			ValueParam vp = (ValueParam) p;
			if (vp.getValue() instanceof List) {
				List vals = (List) vp.getValue();
				String[] values = new String[vals.size()];
				vals.toArray(values);
				return values;
			} else {
				return new String[] { (String) vp.getValue() };
			}
		} else {
			return super.getParameterValues(name);
		}
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		if (parameters == null) {
			parseRequest();
		}

		Map<String, String[]> params = new HashMap<String, String[]>(super.getParameterMap());

		for(Map.Entry<String, Param> entry: parameters.entrySet()) {
			String name = entry.getKey();
			Param p = entry.getValue();
			if (p instanceof ValueParam) {
				ValueParam vp = (ValueParam) p;
				if (vp.getValue() instanceof String) {
					params.put(name, new String[] { (String) vp.getValue() });
				} else if (vp.getValue() instanceof List) {
					params.put(name, getParameterValues(name));
				}
			}
		}
		
		return params;
	}
}

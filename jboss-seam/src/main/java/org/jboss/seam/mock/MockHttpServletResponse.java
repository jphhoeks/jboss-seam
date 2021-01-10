package org.jboss.seam.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

	private List<Cookie> cookies = new ArrayList<>();
	private Map<String, Collection<String>> headers = new HashMap<>();
	private int status = HttpServletResponse.SC_OK;
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private Charset charset = Charset.defaultCharset();
	private Locale locale = Locale.getDefault();
	private String contentType = "text/html";

	public MockHttpServletResponse() {
		super();
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public void addDateHeader(String name, long date) {
		addHeader(name, getDate(date));
	}

	@Override
	public void addHeader(String key, String value) {
		if (!headers.containsKey(key)) {
			headers.put(key, new ArrayList<String>());
		}
		headers.get(key).add(value);
	}

	@Override
	public void addIntHeader(String key, int value) {
		addHeader(key, Integer.toString(value));
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.containsKey(name);
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String url) {
		return url;
	}

	@Override
	@Deprecated
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	@Override
	public void sendError(int statusCode) throws IOException {
		this.status = statusCode;

	}

	@Override
	public void sendError(int statusCode, String message) throws IOException {
		this.status = statusCode;
	}

	@Override
	public void sendRedirect(String url) throws IOException {
		setHeader("Location", url);
		setStatus(SC_FOUND);
	}

	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, getDate(date));
	}

	@Override
	public void setHeader(String key, String value) {
		headers.put(key, Arrays.asList(value));

	}

	@Override
	public void setIntHeader(String key, int value) {
		setHeader(key, Integer.toString(value));

	}

	@Override
	public void setStatus(int statusCode) {
		this.status = statusCode;
	}

	@Override
	public void flushBuffer() throws IOException {
		baos.flush();
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public String getCharacterEncoding() {
		return this.charset.displayName();
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new DelegatingServletOutputStream(baos);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(new OutputStreamWriter(baos, this.getCharacterEncoding()), true);
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
		baos.reset();

	}

	@Override
	public void resetBuffer() {
		reset();

	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCharacterEncoding(String encoding) {
		this.charset = Charset.forName(encoding);

	}

	@Override
	public void setContentLength(int length) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public String getHeader(String name) {
		Collection<String> col = headers.get(name);
		if (col != null && !col.isEmpty()) {
			return col.iterator().next();
		}
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		Collection<String> retVal = new ArrayList<>();
		Collection<String> data = headers.get(name);
		if (data != null) {
			retVal.addAll(data);
		}
		return retVal;
	}

	@Override
	public Collection<String> getHeaderNames() {
		Collection<String> retVal = new ArrayList<>();
		retVal.addAll(headers.keySet());
		return retVal;
	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub

	}

	@Override
	@Deprecated
	public void setStatus(int statusCode, String message) {
		setStatus(statusCode);

	}

	@Override
	@Deprecated
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getDate(long time) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(time);
	}

}

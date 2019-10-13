package org.jboss.seam.test.unit.web;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.log4j.Logger;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.web.MultipartRequestImpl;
import org.testng.annotations.Test;

public class MultipartRequestImplTest {

	private static final transient Logger log = Logger.getLogger(MultipartRequestImplTest.class);

	private static final String CRLF = "\r\n";
	private static final String HYPHENS = "--";

	@Test
	public void testParseRequestBasic() throws Throwable {
		String boundary = "boundary10"; //10 bytes

		String data = HYPHENS + boundary + CRLF + "Content-Disposition: form-data; name=\"foo\"" + CRLF + CRLF + "bar" + CRLF + HYPHENS
				+ boundary + HYPHENS;

		byte[] dataBytes = data.getBytes("UTF-8");

		EnhancedMockHttpServletRequest req = new EnhancedMockHttpServletRequest();
		req.setContent(dataBytes);
		req.setContentType("multipart/form-data; boundary=" + boundary);
		MultipartRequestImpl r = new MultipartRequestImpl(req, false, 0);
		Map<String, String[]> m = r.getParameterMap();
		assertNotNull(m);
		assertEquals(r.getParameterValues("foo")[0], "bar");
	}

	@Test
	public void testParseRequestBufferBoundary() throws Throwable {

		int bufferSize = 2048; // See MultipartRequestImpl

		String boundary = "boundary10";

		String paddingParameter = HYPHENS + boundary + CRLF + "Content-Disposition: form-data; name=\"padding\"" + CRLF + CRLF;

		String testParameter = HYPHENS + boundary + CRLF + "Content-Disposition: form-data; name=\"foo\"" + CRLF + CRLF + "bar" + CRLF
				+ HYPHENS + boundary + HYPHENS;

		// let's put test parameter near the buffer boundary, from (bufferSize - 100) to (bufferSize + 100)
		for (int i = -100; i < 100; i++) {
			StringBuffer buffer = new StringBuffer(bufferSize + 256);
			buffer.append(paddingParameter);
			int paddingSize = bufferSize - i - paddingParameter.length() - CRLF.length();
			appendPaddingValue(buffer, paddingSize);
			buffer.append(CRLF);
			buffer.append(testParameter);
			String data = buffer.toString();

			byte[] dataBytes = data.getBytes("UTF-8");
			EnhancedMockHttpServletRequest req = new EnhancedMockHttpServletRequest();
			req.setContent(dataBytes);
			req.setContentType("multipart/form-data; boundary=" + boundary);
			MultipartRequestImpl r = new MultipartRequestImpl(req, false, 0);
			Map<String, String[]> m = r.getParameterMap();
			assertNotNull(m);
			assertEquals(r.getParameterValues("foo")[0], "bar");
		}
	}

	private static StringBuffer appendPaddingValue(StringBuffer buffer, int length) {
		for (int i = 0; i < length; i++) {
			buffer.append("x");
		}
		return buffer;
	}

	/**
	* Test for bug JBSEAM-4822 MultipartRequestImpl incorrect parse filename with semicolon
	*/
	@Test
	public void testFileNameWithSemicolon() {
		EnhancedMockHttpServletRequest req = new EnhancedMockHttpServletRequest();
		MultipartRequestImpl r = new MultipartRequestImpl(req, false, 0);
		Map<String, String> headers = r.parseParams("Content-Disposition: form-data; name=\"form:fileUpload\"; filename=\"x;a.txt\"", ";");
		assertEquals(3, headers.size());
		assertEquals(headers.get("Content-Disposition"), "form-data");
		assertEquals(headers.get("name"), "form:fileUpload");
		assertEquals(headers.get("filename"), "x;a.txt");

	}

	/**
	* Test for bug JBSEAM-3897 MultipartRequest Error: 100% CPU (for 2.0.2.GA) or Missing data
	*/
	@Test
	public void testParseRequestMissingParam() throws Throwable {

		try {

			final InputStream stream = this.getClass().getClassLoader()
					.getResourceAsStream("org/jboss/seam/test/unit/web/testMultiFileRequest.txt");

			assertNotNull(stream);

			HttpServletRequest req = new EmptyRequest(new ServletInputStream() {

				@Override
				public int read() throws IOException {

					return stream.read();
				}

				@Override
				public boolean isFinished() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isReady() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setReadListener(ReadListener arg0) {
					// TODO Auto-generated method stub

				}
			}, "----WebKitFormBoundaryBtaQkmLRzQZtv7qT");

			MultipartRequestImpl r = new MultipartRequestImpl(req, false, 0);

			Map<String, String[]> m = r.getParameterMap();

			assertNotNull(m);

			Map<String, String> expectedParamValues = new HashMap<String, String>();
			expectedParamValues.put("buttons", "91");
			expectedParamValues.put("form", "form");
			expectedParamValues.put("form:viceSubjektuYFlSf-nazev-dokumentu-field:viceSubjektuYFlSf-nazev-dokumentu", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-druh-exekucniho-titulu-field:viceSubjektuYFlSf-druh-exekucniho-titulu",
					"org.jboss.seam.ui.NoSelectionConverter.noSelectionValue");
			expectedParamValues.put("form:viceSubjektuYFlSf-typ-exekucniho-prikazu-field:viceSubjektuYFlSf-typ-exekucniho-prikazu", "1");
			expectedParamValues.put("form:viceSubjektuYFlSf-ecj-field:viceSubjektuYFlSf-ecj", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-popis-field:viceSubjektuYFlSf-popis", "");
			expectedParamValues.put("viceSubjektuYFlSf-subjektAdresa", "");
			expectedParamValues.put("viceSubjektuYFlSf-druh", "1");
			// expectedParamValues.put("uploaded-Files", null);
			expectedParamValues.put("uploaded-files", null);

			expectedParamValues.put("form:viceSubjektuYFlSf-jmeno-field:viceSubjektuYFlSf-jmeno", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni3",
					"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx13y");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni2",
					"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx12y");
			expectedParamValues.put("form:viceSubjektuYFlSf-titulyPred-field:viceSubjektuYFlSf-titulyPred", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-titulyZa-field:viceSubjektuYFlSf-titulyZa", "\r\nljhkflkjlljh");

			for (Object paramNameO : Collections.list(r.getParameterNames())) {
				String paramName = (String) paramNameO;
				String expectedParamValue = expectedParamValues.get(paramName);
				if (log.isDebugEnabled()) {
					log.debug("Expected [" + paramName + "]->[" + expectedParamValue + "], got [" + r.getParameterValues(paramName) + "]");
				}

				assertTrue(expectedParamValues.containsKey(paramName), "Unexpected parameter [" + paramName + "] found in request");

				String[] realValue = r.getParameterValues(paramName);

				if (expectedParamValue == null) {
					assertNull(realValue);
				} else {
					assertNotNull(realValue,
							"Param [" + paramName + "]: expected to have value [" + expectedParamValue + "], but got null");
					assertEquals(realValue.length, 1);
					assertEquals(realValue[0], expectedParamValue, "For param [" + paramName + "]");
				}
				expectedParamValues.remove(paramName);
			}

			if (expectedParamValues.size() > 0) {
				log.error("" + expectedParamValues.size() + " expected parameters not parsed correctly:");
				for (Entry<String, String> en : expectedParamValues.entrySet()) {

					log.error("Expected parameter [" + en.getKey() + "], value [" + en.getValue() + "]");
				}
			}
			assertEquals(expectedParamValues.size(), 0);

			byte[] b = r.getFileBytes("uploaded-files");

			assertNotNull(b);

		} catch (final Throwable e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

	/**
	* Test for bug JBSEAM-3897 MultipartRequest Error: 100% CPU (for 2.0.2.GA) or Missing data
	*/
	@Test
	public void testParseRequestLoop() throws Throwable {

		try {

			final InputStream stream = this.getClass().getClassLoader()
					.getResourceAsStream("org/jboss/seam/test/unit/web/testMultiFileRequest2.txt");

			assertNotNull(stream);

			HttpServletRequest req = new EmptyRequest(new ServletInputStream() {
				@Override
				public int read() throws IOException {
					return stream.read();
				}

				@Override
				public boolean isFinished() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public boolean isReady() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void setReadListener(ReadListener arg0) {
					// TODO Auto-generated method stub

				}
			}, "----WebKitFormBoundaryBtaQkmLRzQZtv7qT");

			MultipartRequestImpl r = new MultipartRequestImpl(req, false, 0);

			Map<String, String[]> m = r.getParameterMap();

			assertNotNull(m);

			Map<String, String> expectedParamValues = new HashMap<String, String>();
			expectedParamValues.put("buttons", "91");
			expectedParamValues.put("form", "form");
			expectedParamValues.put("form:viceSubjektuYFlSf-nazev-dokumentu-field:viceSubjektuYFlSf-nazev-dokumentu", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-druh-exekucniho-titulu-field:viceSubjektuYFlSf-druh-exekucniho-titulu",
					"org.jboss.seam.ui.NoSelectionConverter.noSelectionValue");
			expectedParamValues.put("form:viceSubjektuYFlSf-typ-exekucniho-prikazu-field:viceSubjektuYFlSf-typ-exekucniho-prikazu", "1");
			expectedParamValues.put("form:viceSubjektuYFlSf-ecj-field:viceSubjektuYFlSf-ecj", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-popis-field:viceSubjektuYFlSf-popis", "");
			expectedParamValues.put("viceSubjektuYFlSf-subjektAdresa", "");
			expectedParamValues.put("viceSubjektuYFlSf-druh", "1");
			// expectedParamValues.put("uploaded-Files", null);
			expectedParamValues.put("uploaded-files", null);

			expectedParamValues.put("form:viceSubjektuYFlSf-jmeno-field:viceSubjektuYFlSf-jmeno", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni3",
					"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx13y");
			expectedParamValues.put("form:viceSubjektuYFlSf-prijmeni-field:viceSubjektuYFlSf-prijmeni2",
					"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx12y");
			expectedParamValues.put("form:viceSubjektuYFlSf-titulyPred-field:viceSubjektuYFlSf-titulyPred", "");
			expectedParamValues.put("form:viceSubjektuYFlSf-titulyZa-field:viceSubjektuYFlSf-titulyZa", "\r\nljhkflfjlljh");

			for (Object paramNameO : Collections.list(r.getParameterNames())) {
				String paramName = (String) paramNameO;
				String expectedParamValue = expectedParamValues.get(paramName);
				if (log.isDebugEnabled()) {
					log.debug("Expected [" + paramName + "]->[" + expectedParamValue + "], got [" + r.getParameterValues(paramName) + "]");
				}

				assertTrue(expectedParamValues.containsKey(paramName), "Unexpected parameter [" + paramName + "] found in request");

				String[] realValue = r.getParameterValues(paramName);

				if (expectedParamValue == null) {
					assertNull(realValue);
				} else {
					assertNotNull(realValue,
							"Param [" + paramName + "]: expected to have value [" + expectedParamValue + "], but got null");
					assertEquals(realValue.length, 1);
					assertEquals(realValue[0], expectedParamValue, "For param [" + paramName + "]");
				}
				expectedParamValues.remove(paramName);
			}

			if (expectedParamValues.size() > 0) {
				log.error("" + expectedParamValues.size() + " expected parameters not parsed correctly:");
				for (Entry<String, String> en : expectedParamValues.entrySet()) {

					log.error("Expected parameter [" + en.getKey() + "], value [" + en.getValue() + "]");
				}
			}
			assertEquals(expectedParamValues.size(), 0);
			byte[] b = r.getFileBytes("uploaded-files");
			assertNotNull(b);

		} catch (final Throwable e) {
			log.error(e.getMessage(), e);
			throw e;
		}

	}

}

class EmptyRequest implements HttpServletRequest {

	private final ServletInputStream stream;
	private final String separator;

	public EmptyRequest(ServletInputStream stream, final String separator) {
		this.stream = stream;
		this.separator = separator;
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String[]> getParameterMap() {

		return Collections.emptyMap();
	}

	@Override
	public String getParameter(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return stream;

	}

	@Override
	public String getContentType() {

		return "multipart/form-data; boundary=" + separator;
	}

	@Override
	public int getContentLength() {
		// TODO Auto-generated method stub
		return 20452;
	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@Deprecated
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Enumeration<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAsyncSupported() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getContentLengthLong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String changeSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}
}

/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.mock;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.jboss.seam.util.EnumerationIterator;

/**
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @author Marek Novotny
 * @version $Revision: 13963 $
 */
public class MockExternalContext extends ExternalContext {
	private ServletContext context;

	private HttpServletRequest request;

	private HttpServletResponse response;

	public MockExternalContext() {
		this.context = new MockServletContext();
		this.request = new MockHttpServletRequest(new MockHttpSession(context));
		this.response = new MockHttpServletResponse();
	}

	public MockExternalContext(ServletContext context) {
		this.context = context;
		this.request = new MockHttpServletRequest(new MockHttpSession(context));
		this.response = new MockHttpServletResponse();
	}

	public MockExternalContext(ServletContext context, HttpSession session) {
		this.context = context;
		this.request = new MockHttpServletRequest(session);
		this.response = new MockHttpServletResponse();
	}

	public MockExternalContext(ServletContext context, HttpServletRequest request) {
		this.context = context;
		this.request = request;
		this.response = new MockHttpServletResponse();
	}

	public MockExternalContext(ServletContext context, HttpServletRequest request, HttpServletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
	}

	public MockExternalContext(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public void dispatch(String url) throws IOException {

	}

	@Override
	public String encodeActionURL(String url) {
		return encodeURL(url);
	}

	@Override
	public String encodeNamespace(String ns) {
		return ns;
	}

	@Override
	public String encodeResourceURL(String url) {
		return encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String baseUrl, Map<String, List<String>> parameters) {
		return encodeURL(baseUrl);
	}

	@Override
	public Map<String, Object> getApplicationMap() {
		return new AttributeMap<String, Object>() {
			@Override
			public Enumeration<String> keys() {
				return context.getAttributeNames();
			}

			@Override
			public Object getAttribute(String key) {
				return context.getAttribute(key);
			}

			@Override
			public void setAttribute(String key, Object value) {
				context.setAttribute(key, value);
			}

			@Override
			public void removeAttribute(String key) {
				context.removeAttribute(key);
			}
		};
	}

	@Override
	public String getAuthType() {
		return request.getAuthType();
	}

	@Override
	public Object getContext() {
		return context;
	}

	@Override
	public String getMimeType(String file) {
		return context.getMimeType(file);
	}

	@Override
	public String getInitParameter(String name) {
		return context.getInitParameter(name);
	}

	@Override
	public Map getInitParameterMap() {
		Map result = new HashMap();
		Enumeration e = context.getInitParameterNames();
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			result.put(name, context.getInitParameter(name));
		}
		return result;
	}

	@Override
	public String getRemoteUser() {
		return request.getRemoteUser();
	}

	@Override
	public Object getRequest() {
		return request;
	}

	@Override
	public String getRequestCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	@Override
	public String getRequestContextPath() {
		String path = (String) request.getAttribute("org.jboss.seam.web.requestContextPath");
		return path != null ? path : request.getContextPath();
	}

	@Override
	public Map<String, Object> getRequestCookieMap() {

		Map<String, Object> cookieMap = new HashMap<String, Object>();

		if (request != null && request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}

		return cookieMap;
	}

	@Override
	public Map<String, String> getRequestHeaderMap() {
		Map<String, String> result = new HashMap<String, String>();
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, request.getHeader(name));
		}
		return result;
	}

	@Override
	public Map<String, String[]> getRequestHeaderValuesMap() {
		Map<String, String[]> result = new HashMap<String, String[]>();
		Enumeration<String> en = request.getHeaderNames();
		while (en.hasMoreElements()) {
			String header = en.nextElement();
			List<String> headerList = Collections.list(request.getHeaders(header));
			String[] headers = new String[headerList.size()];
			for (int i = 0; i < headerList.size(); i++) {
				headers[i] = headerList.get(i);
			}
			result.put(header, headers);
		}
		return result;
	}

	@Override
	public Locale getRequestLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public Iterator<Locale> getRequestLocales() {
		return Collections.singleton(Locale.ENGLISH).iterator();
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return new AttributeMap<String, Object>() {
			@Override
			public Enumeration<String> keys() {
				return request.getAttributeNames();
			}

			@Override
			public Object getAttribute(String key) {
				return request.getAttribute(key);
			}

			@Override
			public void setAttribute(String key, Object value) {
				request.setAttribute(key, value);
			}

			@Override
			public void removeAttribute(String key) {
				request.removeAttribute(key);
			}
		};
	}

	@Override
	public Map<String, String> getRequestParameterMap() {
		Map<String, String> map = new HashMap<String, String>();
		Enumeration<String> names = request.getParameterNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			map.put(name, request.getParameter(name));
		}
		return map;
	}

	@Override
	public Iterator<String> getRequestParameterNames() {
		return request.getParameterMap().keySet().iterator();
	}

	@Override
	public Map<String, String[]> getRequestParameterValuesMap() {
		return request.getParameterMap();
	}

	@Override
	public String getRequestPathInfo() {
		String path = (String) request.getAttribute("org.jboss.seam.web.requestPathInfo");
		return path != null ? path : request.getPathInfo();
	}

	@Override
	public String getRequestServletPath() {
		String path = (String) request.getAttribute("org.jboss.seam.web.requestServletPath");
		return path != null ? path : request.getServletPath();
	}

	@Override
	public URL getResource(String name) throws MalformedURLException {
		return context.getResource(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return context.getResourceAsStream(name);
	}

	@Override
	public Set<String> getResourcePaths(String name) {
		return context.getResourcePaths(name);
	}

	@Override
	public Object getResponse() {
		return response;
	}

	@Override
	public Object getSession(boolean create) {
		return request.getSession();
	}

	@Override
	public Map<String, Object> getSessionMap() {
		// TODO: create the session lazily, RI should do that to
		final HttpSession session = request.getSession(true);
		return new AttributeMap<String, Object>() {
			@Override
			public Enumeration<String> keys() {
				return session.getAttributeNames();
			}

			@Override
			public Object getAttribute(String key) {
				return session.getAttribute(key);
			}

			@Override
			public void setAttribute(String key, Object value) {
				session.setAttribute(key, value);
			}

			@Override
			public void removeAttribute(String key) {
				session.removeAttribute(key);
			}
		};
	}

	static abstract class AttributeMap<K, V> implements Map<K, V> {

		public abstract Enumeration<K> keys();

		public V get(Object key) {
			return getAttribute((String) key);
		}

		public V put(Object key, Object value) {
			V result = get(key);
			setAttribute((String) key, value);
			return result;
		}

		public void clear() {
			Enumeration<K> e = keys();
			while (e.hasMoreElements()) {
				remove(e.nextElement());
			}
		}

		public boolean containsKey(Object key) {
			Enumeration<K> e = keys();
			while (e.hasMoreElements()) {
				if (key.equals(e.nextElement())) {
					return true;
				}
			}
			return false;
		}

		public boolean containsValue(Object value) {
			Enumeration<K> e = keys();
			while (e.hasMoreElements()) {
				if (value.equals(get(e.nextElement())))
					return true;
			}
			return false;
		}

		public Set<Map.Entry<K, V>> entrySet() {
			throw new UnsupportedOperationException();
		}

		public abstract V getAttribute(String key);

		public boolean isEmpty() {
			return size() == 0;
		}

		public Set<K> keySet() {
			return new AbstractSet<K>() {

				@Override
				public Iterator<K> iterator() {
					return new EnumerationIterator<K>(keys());
				}

				@Override
				public int size() {
					return AttributeMap.this.size();
				}

			};
		}

		public abstract void setAttribute(String key, Object value);

		public abstract void removeAttribute(String key);

		public void putAll(Map<? extends K, ? extends V> t) {
			for (Map.Entry<? extends K, ? extends V> me : t.entrySet()) {
				put(me.getKey(), me.getValue());
			}
		}

		public V remove(Object key) {
			V result = getAttribute((String) key);
			removeAttribute((String) key);
			return result;
		}

		public int size() {
			int i = 0;
			Enumeration<K> e = keys();
			while (e.hasMoreElements()) {
				e.nextElement();
				i++;
			}
			return i;
		}

		public Collection<V> values() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		return request.isUserInRole(role);
	}

	@Override
	public void log(String message, Throwable t) {

	}

	@Override
	public void log(String t) {
	}

	@Override
	public void redirect(String url) throws IOException {
		if ("partial/ajax".equals(this.request.getHeader("Faces-Request")) && !this.response.isCommitted()) {
			this.response.reset(); // reset any data to not duplicate any response
			this.response.setContentType("text/xml");
			this.response.setCharacterEncoding("UTF-8");
			this.response.addHeader("Cache-Control", "no-cache");
			this.response.setStatus(HttpServletResponse.SC_OK);

			final Document body = DocumentFactory.getInstance().createDocument("UTF-8");
			body.addElement("partial-response").addElement("redirect").addAttribute("url", url);

			this.response.getWriter().format(body.asXML());
			this.response.getWriter().flush();
		} else {
			this.response.sendRedirect(url);
		}

		FacesContext.getCurrentInstance().responseComplete();
	}

	@Override
	public void setRequest(Object myrequest) {
		this.request = (HttpServletRequest) myrequest;
	}

	/**
	 * @since 1.2
	 */
	@Override
	public String getResponseContentType() {
		return response.getContentType();
	}

	/**
	 * Attempt to encode the URL, falling back to an identity function if the
	 * response has not been set on this mock context. This functionality is needed
	 * in order for the ExceptionFilter to maintain the session id when url
	 * rewriting is used.
	 */
	protected String encodeURL(String url) {
		if (response != null) {
			String encodedUrl = response.encodeURL(url);
			url = (encodedUrl != null ? encodedUrl : url);
		}
		return url;
	}
}

/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.servlet;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Abstracts the servlet API specific session context
 * as a Map. Actual sessions are created lazily.
 * 
 * @author Gavin King
 */
public class ServletRequestSessionMap implements Map<String, Object> {
	private HttpServletRequest request;

	public ServletRequestSessionMap(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		HttpSession session = request.getSession(false);
		return session != null && session.getAttribute((String) key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(Object key) {
		HttpSession session = request.getSession(false);
		return session == null ? null : session.getAttribute((String) key);
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return Collections.emptySet();
		} else {
			Set<String> keys = new HashSet<String>();
			Enumeration<String> names = session.getAttributeNames();
			while (names.hasMoreElements()) {
				keys.add(names.nextElement());
			}
			return keys;
		}
	}

	@Override
	public Object put(String key, Object value) {
		HttpSession session = request.getSession(true);
		Object result = session.getAttribute(key);
		session.setAttribute(key, value);
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Object key) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		} else {
			Object result = session.getAttribute((String) key);
			session.removeAttribute((String) key);
			return result;
		}
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}

}

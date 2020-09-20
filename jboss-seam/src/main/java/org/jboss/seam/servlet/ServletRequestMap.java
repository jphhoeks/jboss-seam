/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.servlet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

/**
 * Abstracts the servlet API specific request context
 * as a Map.
 * 
 * @author Gavin King
 */
public class ServletRequestMap implements Map<String, Object> {

	private ServletRequest request;

	public ServletRequestMap(ServletRequest request) {
		this.request = request;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return request.getAttribute((String) key) != null;
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
		return request.getAttribute((String) key);
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		Set<String> keys = new HashSet<String>();
		Enumeration<String> names = request.getAttributeNames();
		while (names.hasMoreElements()) {
			keys.add(names.nextElement());
		}
		return keys;
	}

	@Override
	public Object put(String key, Object value) {
		Object result = request.getAttribute(key);
		request.setAttribute(key, value);
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Object key) {
		Object result = request.getAttribute((String) key);
		request.removeAttribute((String) key);
		return result;
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

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

import javax.servlet.ServletContext;

/**
 * Abstracts the servlet API specific application context
 * as a Map.
 * 
 * @author Gavin King
 */
public class ServletApplicationMap implements Map<String, Object> {
	private ServletContext servletContext;

	public ServletApplicationMap(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		return servletContext.getAttribute((String) key) != null;
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
		return servletContext.getAttribute((String) key);
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		Set<String> keys = new HashSet<String>();
		Enumeration<String> names = servletContext.getAttributeNames();
		while (names.hasMoreElements()) {
			keys.add(names.nextElement());
		}
		return keys;
	}

	@Override
	public Object put(String key, Object value) {
		Object result = servletContext.getAttribute(key);
		servletContext.setAttribute(key, value);
		return result;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object remove(Object key) {
		Object result = servletContext.getAttribute((String) key);
		servletContext.removeAttribute((String) key);
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

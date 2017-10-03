/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.mock;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.jboss.seam.util.IteratorEnumeration;

/**
 * @author Gavin King
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision: 9668 $
 */

public class MockHttpSession implements HttpSession {

	private Map<String, Object> attributes = new HashMap<String, Object>();
	private boolean isInvalid;
	private ServletContext servletContext;
	private int maxInactiveInterval;
	private long creationTime;
	private String id;

	public MockHttpSession() {
		this(new MockServletContext());		
	}

	public MockHttpSession(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.creationTime = System.currentTimeMillis();
		this.id = UUID.randomUUID().toString();
	}


	@Override
	public long getCreationTime() {
		return this.creationTime;
	}
	@Override
	public String getId() {
		return this.id;
	}
	@Override
	public long getLastAccessedTime() {
		return this.creationTime;
	}
	@Override
	public void setMaxInactiveInterval(int max) {
		maxInactiveInterval = max;
	}
	@Override
	public int getMaxInactiveInterval() {
		return maxInactiveInterval;
	}
	@Override
	public Object getAttribute(String att) {
		return attributes.get(att);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IteratorEnumeration<String>(attributes.keySet().iterator());
	}

	@Override
	public void setAttribute(String att, Object value) {
		if (value == null) {
			attributes.remove(att);
		} else {
			attributes.put(att, value);
		}
	}

	@Override
	public void removeAttribute(String att) {
		attributes.remove(att);
	}
	
	@Override
	public void invalidate() {
		attributes.clear();
		isInvalid = true;
	}
	@Override
	public boolean isNew() {
		return false;
	}
	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}
	public boolean isInvalid() {
		return this.isInvalid;
	}

	
	
	@Override
	@Deprecated
	public void putValue(String att, Object value) {
		setAttribute(att, value);
	}
	@Override
	@Deprecated
	public void removeValue(String att) {
		removeAttribute(att);
	}
	@Override
	@Deprecated
	public Object getValue(String att) {
		return getAttribute(att);
	}
	@Override
	@Deprecated
	public String[] getValueNames() {
		return attributes.keySet().toArray(new String[0]);
	}
	@Override
	@Deprecated
	public javax.servlet.http.HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException();
	}

}

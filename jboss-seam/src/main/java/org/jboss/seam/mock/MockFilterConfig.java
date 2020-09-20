package org.jboss.seam.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import org.jboss.seam.util.IteratorEnumeration;

/**
 * @author Gavin King
 */
public class MockFilterConfig implements FilterConfig {
	private ServletContext servletContext;

	public MockFilterConfig(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public String getFilterName() {
		return "Seam Filter";
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public String getInitParameter(String name) {
		return null;
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		List<String> list = Collections.emptyList();
		return new IteratorEnumeration<String>(list.iterator());
	}
}
package org.jboss.seam.servlet;

import org.jboss.seam.web.ContextFilter;

/**
 * Manages the Seam contexts associated with a request to any servlet.
 * 
 * @deprecated use ContextFilter
 * @author Gavin King
 */
@Deprecated
public class SeamServletFilter extends ContextFilter {
	public SeamServletFilter() {
		super();
	}

}

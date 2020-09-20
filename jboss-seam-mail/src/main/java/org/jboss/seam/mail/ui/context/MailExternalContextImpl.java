/**
 * 
 */
package org.jboss.seam.mail.ui.context;

import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

public class MailExternalContextImpl extends ExternalContextWrapper {

	private ExternalContext delegate;
	private String urlBase;

	public MailExternalContextImpl(ExternalContext delegate) {
		this(delegate, null);
	}

	public MailExternalContextImpl(ExternalContext delegate, String urlBase) {
		this.delegate = delegate;
		this.urlBase = urlBase;
	}

	@Override
	public ExternalContext getWrapped() {
		return delegate;
	}

	@Override
	public String getRequestContextPath() {
		if (urlBase == null) {
			return super.getRequestContextPath();
		}
		return urlBase;
	}
}
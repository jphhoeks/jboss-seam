package org.jboss.seam.jsf;

import javax.faces.context.*;

/**
 * Implementation of FacesContext that delegates all calls.
 * 
 * Further, it exposes {@link #setCurrentInstance(FacesContext)} as a public
 * method
 * 
 * @author Pete Muir
 *
 */
public class DelegatingFacesContext extends FacesContextWrapper {

	FacesContext wrapped;

	public DelegatingFacesContext(FacesContext wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public FacesContext getWrapped() {
		return wrapped;
	}

	public FacesContext getDelegate() {
		return wrapped;
	}

	public static void setCurrentInstance(FacesContext context) {
		FacesContext.setCurrentInstance(context);
	}
}

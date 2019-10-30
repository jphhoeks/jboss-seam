package org.jboss.seam.jsf;

import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.render.RenderKit;

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
		this.wrapped = wrapped;
	}

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

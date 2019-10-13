//$Id: HttpError.java 5350 2007-06-20 17:53:19Z gavin $
package org.jboss.seam.faces;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.nio.charset.StandardCharsets;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Convenient HTTP errors
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.faces.httpError")
@Install(precedence = BUILT_IN, classDependencies = "javax.faces.context.FacesContext")
public class HttpError {

	private static final LogProvider log = Logging.getLogProvider(HttpError.class);

	/**
	 * Send a HTTP error as the response
	 */
	public void send(int code) {
		send(code, Integer.toString(code));
	}

	/**
	 * Send a HTTP error as the response
	 */
	public void send(int code, String message) {
		try {
			HttpServletResponse response = getResponse();
			if (!response.isCommitted()) {
				response.reset();
				response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8);
				response.setStatus(code);
				response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
			} else {
				log.warn("Cannot send error because response is already commited");
			}
		} catch (Exception e) {
			log.warn("Cannot send error because and exception ocurred:" + e.getMessage());
		}
		FacesContext.getCurrentInstance().responseComplete();
	}

	private static HttpServletResponse getResponse() {
		return (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
	}

	public static HttpError instance() {
		if (!Contexts.isApplicationContextActive()) {
			throw new IllegalStateException("No active application scope");
		}
		return (HttpError) Component.getInstance(HttpError.class, ScopeType.APPLICATION);
	}

}

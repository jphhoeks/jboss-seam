package org.jboss.seam.exception;

import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.RedirectException;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.Strings;

/**
 * Base implementation of redirection exception handlers.
 * 
 * @author Gavin King
 *
 */
public abstract class RedirectHandler extends ExceptionHandler
{

   private static final LogProvider log = Logging.getLogProvider(RedirectHandler.class);

   protected abstract String getViewId(Exception e);
   protected abstract String getMessage(Exception e);
   protected abstract boolean isEnd(Exception e);
   protected abstract Severity getMessageSeverity(Exception e);
   protected abstract boolean isEndBeforeRedirect(Exception e);

   @Override
	public void handle(Exception e) throws Exception {
		String viewId = getViewId(e);

		// we want to perform a redirect straight back to the current page
		// there is no ViewRoot available, so lets do it the hard way
		String servletPath = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest()).getServletPath();
		String currentView = servletPath + Pages.getSuffix();
		if (servletPath.contains(".")) {
			currentView = servletPath.substring(0, servletPath.lastIndexOf('.')) + Pages.getSuffix();
		}
		if (viewId == null) {
			viewId = currentView;
		}

		addFacesMessage("#0", getMessageSeverity(e), null, getDisplayMessage(e, getMessage(e)));

		if (Contexts.isConversationContextActive()) {
			if (isEndBeforeRedirect(e)) {
				Conversation.instance().endBeforeRedirect();				
			}
			else if (isEnd(e)) {
				Conversation.instance().end();
			}
		}

		try {
			if (isErrorOrDebugPage(viewId) && isErrorOrDebugPage(currentView)) {
				error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error! Something bad happened :-(");
				return;
			}
			if (is403Page(currentView)) {
				error(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
				return;
			}
			if (is404Page(currentView)) {
				error(HttpServletResponse.SC_NOT_FOUND, "Not found");
				return;
			}
			redirect(viewId, null);
		} catch (RedirectException re) {
			// do nothing
			log.debug("could not redirect", re);
		}
	}

	private boolean is404Page(String viewId) {
		if (Strings.isEmpty(viewId)) {
			return false;
		}
		return viewId.equals("/404" + Pages.getSuffix()) ;
	}

	private boolean is403Page(String viewId) {
		if (Strings.isEmpty(viewId)) {
			return false;
		}
		return viewId.equals("/403" + Pages.getSuffix());
	}
	private boolean isErrorOrDebugPage(String viewId) {
		if (Strings.isEmpty(viewId)) {
			return false;
		}
		return viewId.equals("/error" + Pages.getSuffix()) || viewId.equals("/debug" + Pages.getSuffix());
	}

	@Override
	public String toString() {
		return "RedirectHandler";
	}
}
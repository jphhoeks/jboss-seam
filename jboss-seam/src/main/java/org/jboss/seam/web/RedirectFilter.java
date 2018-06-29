package org.jboss.seam.web;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;
import static org.jboss.seam.core.Manager.REDIRECT_FROM_MANAGER;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesManager;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.util.Strings;

/**
 * Propagates the conversation context and page parameters across any
 * browser redirect initiated from a JSF navigation rule defined in
 * faces-config.xml. Note that this is no longer needed if all
 * navigation rules are defined in pages.xml.
 *
 * @author Gavin King
 */
@Scope(APPLICATION)
@Name("org.jboss.seam.web.redirectFilter")
@Install(precedence = BUILT_IN, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
@Filter(within="org.jboss.seam.web.ajax4jsfFilter", around="org.jboss.seam.web.exceptionFilter")
public class RedirectFilter extends AbstractFilter
{
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
         FilterChain chain) throws IOException, ServletException
    {
        RedirectFilterHttpResponseWrapper r = new RedirectFilterHttpResponseWrapper( (HttpServletResponse)response );
        chain.doFilter( request, r );
        if (! (isErrorOrDebugOrLoginPage(r.getSavedUrl()) && isErrorOrDebugOrLoginPage(request))) {        	
        	r.completeRedirect();
        }
        else {
        	r.sendError("Error! Something bad happened :-(");
        }
    }
    private boolean isErrorOrDebugOrLoginPage(String savedUrl) {
    	if (Strings.isEmpty(savedUrl)) {
    		return false;
    	}
    	return 
    			savedUrl.contains("/debug.") || 
    			savedUrl.contains("/error.") || 
    			savedUrl.contains("/login.") ||
    			savedUrl.contains("/404.") ||
    			savedUrl.contains("/403.");
	}

	private boolean isErrorOrDebugOrLoginPage(ServletRequest request) {
		String url = "";
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			url = req.getRequestURI();
		}
		return isErrorOrDebugOrLoginPage(url);
	}
   public static String getViewId(String url)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      if (facesContext==null)
      {
         return null;
      }
      else
      {
         ExternalContext externalContext = facesContext.getExternalContext();
         String pathInfo = externalContext.getRequestPathInfo();
         String servletPath = externalContext.getRequestServletPath();
         String contextPath = externalContext.getRequestContextPath();
         return getViewId(url, pathInfo, servletPath, contextPath);
      }
   }

   protected static String getViewId(String url, String pathInfo, String servletPath, String contextPath)
   {
      if (pathInfo!=null)
      {
         //for /seam/* style servlet mappings
         return url.substring( contextPath.length() + servletPath.length(), getParamLoc(url) );
      }
      else if ( url.startsWith(contextPath) )
      {
         //for *.seam style servlet mappings
         String extension = servletPath.substring( servletPath.lastIndexOf('.') );
         if ( url.endsWith(extension) || url.contains(extension + '?') )
         {
            String suffix = Pages.getSuffix();
            return url.substring( contextPath.length(), getParamLoc(url) - extension.length() ) + suffix;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

	private static int getParamLoc(String url) {
		int loc = url.indexOf('?');
		if (loc < 0) {
			loc = url.length();
		}
		return loc;
	}
    /**
     * Seam wraps transactions around the InvokeApplication phase.  If a sendRedirect is called
     * on the FacesContext during that phase,  the browser will receive the redirect location BEFORE the invoke
     * application phase is completed (and before seam commits its transaction).
     * This class wraps the response object and will store the redirect url to be sent at a later time (when the
     * doFilter is complete)
     */
    public static class RedirectFilterHttpResponseWrapper extends HttpServletResponseWrapper
    {
    	private static final LogProvider log = Logging.getLogProvider(RedirectFilter.class);
        private String savedUrl;

        public RedirectFilterHttpResponseWrapper( HttpServletResponse response )
        {
            super( response );
        }

        @Override
        public void sendRedirect( String url ) throws IOException
        {
            if ( FacesContext.getCurrentInstance() != null
                    && Contexts.isEventContextActive()
                    && !Contexts.getEventContext().isSet( REDIRECT_FROM_MANAGER ) )
            {
                if ( !url.startsWith( "http:" ) && !url.startsWith( "https:" ) ) //yew!
                {
                    String viewId = getViewId( url );
                    if ( viewId != null )
                    {
                        url = Pages.instance().encodePageParameters( FacesContext.getCurrentInstance(), url, viewId );
                    }
                    if ( Contexts.isConversationContextActive() )
                    {
                        url = FacesManager.instance().appendConversationIdFromRedirectFilter( url, viewId );
                    }
                }
            }
            this.savedUrl = url;
        }

        public void completeRedirect() throws IOException
        {
            if(savedUrl != null) {
            	if (!isCommitted()) {
            		super.sendRedirect( savedUrl );
            	}
            	else {
            		log.warn("Cannot redirect because response is already commited");
            	}
            }
        }

        public String getSavedUrl()
        {
            return savedUrl;
        }
        public void sendError(String message) {
        	if (!isCommitted()) {
        		reset();
        		setStatus(SC_INTERNAL_SERVER_ERROR);
        		try {
					getOutputStream().write(message.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
        	}
        }

   }

}

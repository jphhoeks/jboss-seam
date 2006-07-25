package org.jboss.seam.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.security.config.SecurityConfig;
import org.jboss.seam.security.config.SecurityConfigException;
import org.jboss.seam.security.config.SecurityConfigFileLoader;

/**
 * A servlet filter that performs authentication within a Seam application.
 *
 * @author Shane Bryzak
 */
public class SeamSecurityFilter implements Filter
{
  private static final Log log = LogFactory.getLog(SeamSecurityFilter.class);
  private ServletContext servletContext;

  private static final String CONFIG_RESOURCE = "/WEB-INF/seam-security.xml";

  public void init(FilterConfig config)
      throws ServletException
  {
    servletContext = config.getServletContext();

    try
    {
      Lifecycle.setServletContext(servletContext);
      Lifecycle.beginCall();
      SecurityConfig.instance().loadConfig(new SecurityConfigFileLoader(
        servletContext.getResourceAsStream(CONFIG_RESOURCE), servletContext));
      Contexts.getApplicationContext().set("org.jboss.seam.security.realm.Realm",
                                           SecurityConfig.instance().getRealm());
    }
    catch (SecurityConfigException ex)
    {
      log.error(ex);
      throw new ServletException("Error loading security configuration", ex);
    }
    catch (Exception ex)
    {
      throw new ServletException(ex);
    }
    finally
    {
      Lifecycle.endCall();
    }
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException
  {
//     HttpSession session = ( (HttpServletRequest) request ).getSession(true);

     HttpServletRequest hRequest = (HttpServletRequest) request;
     HttpServletResponse hResponse = (HttpServletResponse) response;

     try
     {
       if (SecurityConfig.instance().getAuthenticator().processLogin(hRequest, hResponse))
         return;

       chain.doFilter(request, response);
     }
     catch (Exception e)
     {
       if (e instanceof ServletException)
       {
         Throwable cause = ((ServletException) e).getRootCause();

         // Is there a better way?
         Set<Throwable> causes = new HashSet<Throwable>();
         while (cause != null && !causes.contains(cause))
         {
           if (cause instanceof FailedLoginException)
           {
             // Redirect to login page
             log.info("User not logged in... redirecting to login page.");

             SecurityConfig.instance().getAuthenticator().showLogin(hRequest, hResponse);
             break;
           }
           causes.add(cause);
           cause = cause.getCause();
         }
       }

       throw new ServletException(e);
     }
  }

  public void destroy() {}
}

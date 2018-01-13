package org.jboss.seam.webservice;

import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.annotations.intercept.InterceptorType;
import org.jboss.seam.async.AsynchronousInterceptor;
import org.jboss.seam.security.SecurityInterceptor;

/**
 * Provides authorization services for web service invocations.
 * 
 * @author Shane Bryzak
 */
@Interceptor(stateless = true, type=InterceptorType.SERVER, 
         around=AsynchronousInterceptor.class)
public class WSSecurityInterceptor extends SecurityInterceptor
{
	private static final long serialVersionUID = 4096942998268335354L;

	@Override
   public boolean isInterceptorEnabled()
   {
      return getComponent().isSecure() && getComponent().beanClassHasAnnotation("javax.jws.WebService");
   }
   
}

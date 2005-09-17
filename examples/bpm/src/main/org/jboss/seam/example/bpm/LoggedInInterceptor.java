//$Id$
package org.jboss.seam.example.bpm;

import javax.ejb.AroundInvoke;
import javax.ejb.InvocationContext;

import org.jboss.logging.Logger;
import org.jboss.seam.annotations.Around;
import org.jboss.seam.annotations.Within;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.interceptors.BijectionInterceptor;
import org.jboss.seam.interceptors.ConversationInterceptor;
import org.jboss.seam.interceptors.RemoveInterceptor;
import org.jboss.seam.interceptors.ValidationInterceptor;
import org.jbpm.security.Authentication;

@Around( { BijectionInterceptor.class, ValidationInterceptor.class, ConversationInterceptor.class } )
@Within( RemoveInterceptor.class )
public class LoggedInInterceptor
{
   private static final Logger log = Logger.getLogger( LoggedInInterceptor.class );

   @AroundInvoke
   public Object checkLoggedIn(InvocationContext invocation) throws Exception
   {
      User user = ( User ) Contexts.getSessionContext().get( "user" );
      if ( user == null || user.getUsername() == null )
      {
         return "login";
      }

      Authentication.pushAuthenticatedActorId( user.getUsername() );
      try
      {
         return invocation.proceed();
      }
      finally
      {
         Authentication.popAuthenticatedActorId();
      }
   }

}

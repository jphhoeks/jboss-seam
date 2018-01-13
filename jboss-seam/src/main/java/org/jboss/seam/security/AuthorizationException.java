package org.jboss.seam.security;

import javax.ejb.ApplicationException;

/**
 * Thrown when an authenticated user has insufficient rights to carry out an action.
 * 
 * @author Shane Bryzak
 */
@ApplicationException(rollback=true)
public class AuthorizationException extends RuntimeException
{ 
   private static final long serialVersionUID = -981091398588455903L;

public AuthorizationException(String message)
   {
      super(message);
   }
}

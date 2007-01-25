package org.jboss.seam.security;

/**
 * Delegating wrapper for EL security functions.
 * 
 * @author Shane Bryzak
 */
public class SecurityFunctions
{
   public static boolean hasRole(String name)
   {
      return Identity.instance().hasRole(name);
   }
   
   public static boolean hasPermission(String name, String action,
            Object arg)
   {
      return Identity.instance().hasPermission(name, action, arg);
   }
}

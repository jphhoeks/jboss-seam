/**
 * 
 */
package org.jboss.seam;

public class ConcurrentRequestTimeoutException extends RuntimeException
{

   private static final long serialVersionUID = -4512875941750602410L;

public ConcurrentRequestTimeoutException()
   {
      super();
   }

   public ConcurrentRequestTimeoutException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ConcurrentRequestTimeoutException(String message)
   {
      super(message);
   }

   public ConcurrentRequestTimeoutException(Throwable cause)
   {
      super(cause);
   }
   
}
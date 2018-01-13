package org.jboss.seam.web;

/**
 * Thrown when an exception occurs while uploading a file. 
 *  
 * @author Shane Bryzak
 */
public class FileUploadException extends RuntimeException
{   
   private static final long serialVersionUID = 1L;

public FileUploadException()
   {
      this(null, null);
   }
   
   public FileUploadException(String message)
   {
      this(message, null);
   }
   
   public FileUploadException(String message, Throwable cause)
   {
      super(message, cause);
   }   
}

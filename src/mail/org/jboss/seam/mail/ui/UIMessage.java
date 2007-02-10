package org.jboss.seam.mail.ui;

import java.io.IOException;

import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jboss.seam.mail.MailSession;
import org.jboss.seam.mail.ui.context.MailFacesContextImpl;

/**
 * JSF component which delimites the start and end of the mail message.
 */
public class UIMessage extends MailComponent
{

   public static class Importance
   {

      public static final String LOW = "low";

      public static final String NORMAL = "normal";

      public static final String HIGH = "high";

   }

   private MimeMessage mimeMessage;

   private Session session;

   private String importance;

   private String precedence;

   private Boolean requestReadReceipt;

   private String urlBase;

   private String absoluteUrlBase;

   /**
    * Get the JavaMail Session to use. If not set the default session is used
    */
   public Session getMailSession()
   {
      if (session == null)
      {
         if (getValue("session") != null)
         {
            session = (Session) getValue("session");
         }
         else
         {
            session = MailSession.instance();
         }
      }
      return session;
   }

   public void setMailSession(Session session)
   {
      this.session = session;
   }

   public MimeMessage getMimeMessage() throws MessagingException
   {
      if (mimeMessage == null)
      {
         mimeMessage = new MimeMessage(getMailSession());
         Multipart root = new MimeMultipart();
         mimeMessage.setContent(root);
      }
      return mimeMessage;
   }

   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if ( getAbsoluteUrlBase() != null ) 
      {
         MailFacesContextImpl.start( getAbsoluteUrlBase() );
      } 
      else if ( getUrlBase() != null ) 
      {
         MailFacesContextImpl.start( getUrlBase() + context.getExternalContext().getRequestContextPath() );
      }
      mimeMessage = null;
      try
      {
         if (Importance.HIGH.equalsIgnoreCase(getImportance()))
         {
            // Various mail client's use different headers for indicating
            // importance
            // This is a common set, more may need to be added.
            getMimeMessage().addHeader("X-Prioity", "1");
            getMimeMessage().addHeader("Priority", "Urgent");
            getMimeMessage().addHeader("Importance", "high");
         }
         else if (Importance.LOW.equalsIgnoreCase(getImportance()))
         {
            getMimeMessage().addHeader("X-Priority", "5");
            getMimeMessage().addHeader("Priority", "Non-urgent");
            getMimeMessage().addHeader("Importance", "low");
         }
         if (getPrecedence() != null)
         {
            getMimeMessage().addHeader("Precedence", getPrecedence());
         }
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
   }

   @Override
   public void encodeEnd(FacesContext ctx) throws IOException
   {
      super.encodeEnd(ctx);
      try
      {
         if (isRequestReadReceipt() && getMimeMessage().getFrom() != null
                  && getMimeMessage().getFrom().length == 1)
         {
            getMimeMessage().addHeader("Disposition-Notification-To",
                     getMimeMessage().getFrom()[0].toString());
         }
         Transport.send(getMimeMessage());
      }
      catch (MessagingException e)
      {
         throw new FacesException(e.getMessage(), e);
      }
      MailFacesContextImpl.stop();
   }

   @Override
   public boolean getRendersChildren()
   {
      return false;
   }

   public String getImportance()
   {
      if (importance == null)
      {
         return getString("importance");
      }
      else
      {
         return importance;
      }
   }

   public void setImportance(String importance)
   {
      this.importance = importance;
   }

   public String getPrecedence()
   {
      if (precedence == null)
      {
         return getString("precedence");
      }
      else
      {
         return precedence;
      }
   }

   public void setPrecedence(String precedence)
   {
      this.precedence = precedence;
   }

   public boolean isRequestReadReceipt()
   {
      if (requestReadReceipt == null)
      {
         return getBoolean("requestReadReceipt") == null ? false : getBoolean("requestReadReceipt");
      }
      else
      {
         return requestReadReceipt;
      }
   }

   public void setRequestReadReceipt(boolean requestReadReceipt)
   {
      this.requestReadReceipt = requestReadReceipt;
   }

   public String getAbsoluteUrlBase()
   {
      if (absoluteUrlBase == null)
      {
         return getString("absoluteUrlBase");
      }
      else
      {
         return absoluteUrlBase;
      }
   }

   public void setAbsoluteUrlBase(String absoluteUrlBase)
   {
      this.absoluteUrlBase = absoluteUrlBase;
   }

   public String getUrlBase()
   {
      if (urlBase == null)
      {
         return urlBase;
      }
      else
      {
         return urlBase;
      }
   }

   public void setUrlBase(String urlBase)
   {
      this.urlBase = urlBase;
   }

}

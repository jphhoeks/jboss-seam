package org.jboss.seam.document;

import java.io.IOException;
import java.net.URLEncoder;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.jsf.SeamApplication;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.web.Parameters;

public class DocumentStorePhaseListener implements PhaseListener
{
   private static final long serialVersionUID = 7308251684939658978L;

   private static final LogProvider log = Logging.getLogProvider(DocumentStorePhaseListener.class);

   public PhaseId getPhaseId()
   {
      return PhaseId.RENDER_RESPONSE;
   }

   public void afterPhase(PhaseEvent phaseEvent)
   {
      // ...
   }

   public void beforePhase(PhaseEvent phaseEvent)
   {
		// Skip non-seam applications
		if (!SeamApplication.isSeamApplication(phaseEvent.getFacesContext())) {
			return;
		}
      String rootId = Pages.getViewId(phaseEvent.getFacesContext());

      Parameters params = Parameters.instance();
      String id = (String) params.convertMultiValueRequestParameter(params.getRequestParameters(), "docId", String.class);
      if (rootId.contains(DocumentStore.DOCSTORE_BASE_URL))
      {
         sendContent(phaseEvent.getFacesContext(), id);
      }
   }

   public void sendContent(FacesContext context, String contentId)
   {
      try
      {
         DocumentData documentData = DocumentStore.instance().getDocumentData(contentId);

         if (documentData != null)
         {

            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
            response.setContentType(documentData.getDocumentType().getMimeType());
            if (isInternetExplorer(request)) {
            	response.setHeader("Content-Disposition",documentData.getDisposition() + "; filename=\"" + URLEncoder.encode(documentData.getFileName(), "utf-8") + "\"");
            }
            else {
            	 response.setHeader("Content-Disposition",documentData.getDisposition() + "; filename=\""  + MimeUtility.encodeWord(documentData.getFileName(), "UTF-8", "Q") + "\"");
            }

            setHeadersForInternetExplorer(request, response);
            documentData.writeDataToStream(response.getOutputStream());
            context.responseComplete();
         }
      }
      catch (IOException e)
      {
         log.warn(e);
      }
   }
   protected static void setHeadersForInternetExplorer(HttpServletRequest request, HttpServletResponse response){
       if (request == null) {
           return;
       }
       if (request.isSecure() && isInternetExplorer(request)){
           response.setHeader("Pragma", "a");
           response.setHeader("Cache-Control", "max-age=0");
           response.addHeader("Cache-Control", "must-revalidate");
       }
   }
   protected static boolean isInternetExplorer (HttpServletRequest request) {        
       if (request == null) {
    	   return false;
       }
       String useragent = request.getHeader("User-Agent");
       boolean isIE = useragent != null && useragent.contains("MSIE");
       return isIE;
   }
}

package org.jboss.seam.remoting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.PhaseId;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Manager;
import org.jboss.seam.remoting.messaging.RemoteSubscriber;
import org.jboss.seam.remoting.messaging.SubscriptionRegistry;
import org.jboss.seam.remoting.messaging.SubscriptionRequest;

/**
 *
 * @author Shane Bryzak
 */
public class SubscriptionHandler extends BaseRequestHandler implements RequestHandler
{

  private ServletContext servletContext;

  public void setServletContext(ServletContext ctx)
  {
    this.servletContext = ctx;
  }

  /**
   * The entry point for handling a request.
   *
   * @param request HttpServletRequest
   * @param response HttpServletResponse
   * @throws Exception
   */
  public void handle(HttpServletRequest request, HttpServletResponse response)
      throws Exception
  {
    // We're sending an XML response, so set the response content type to text/xml
    response.setContentType("text/xml");

    // Parse the incoming request as XML
    SAXReader xmlReader = new SAXReader();
    Document doc = xmlReader.read(request.getInputStream());
    Element env = doc.getRootElement();

    Element body = env.element("body");

    // First handle any new subscriptions
    List<SubscriptionRequest> requests = new ArrayList<SubscriptionRequest>();

    List<Element> elements = body.elements("subscribe");
    for (Element e : elements)
    {
      requests.add(new SubscriptionRequest(e.attributeValue("topic")));
    }

    try
    {
      HttpSession session = ((HttpServletRequest) request).getSession(true);
      Lifecycle.setPhaseId(PhaseId.INVOKE_APPLICATION);
      Lifecycle.setServletRequest(request);
      Lifecycle.beginRequest(servletContext, session);

      Manager.instance().restoreConversation( (String) null );
      Lifecycle.resumeConversation(session);

      for (SubscriptionRequest req : requests)
        req.subscribe();

      // Then handle any unsubscriptions
      List<String> unsubscribeTokens = new ArrayList<String>();

      elements = body.elements("unsubscribe");
      for (Element e : elements) {
        unsubscribeTokens.add(e.attributeValue("token"));
      }

      for (String token : unsubscribeTokens) {
        RemoteSubscriber subscriber = SubscriptionRegistry.instance().
                                      getSubscription(token);
        if (subscriber != null)
          subscriber.unsubscribe();
      }
    }
    finally
    {
      Lifecycle.endRequest();
      Lifecycle.setServletRequest(null);
      Lifecycle.setPhaseId(null);
    }

    // Package up the response
    marshalResponse(requests, response.getOutputStream());
  }

  private void marshalResponse(List<SubscriptionRequest> requests, OutputStream out)
      throws IOException
  {
    out.write(ENVELOPE_TAG_OPEN);
    out.write(BODY_TAG_OPEN);

    for (SubscriptionRequest req : requests)
    {
      req.marshal(out);
    }

    out.write(BODY_TAG_CLOSE);
    out.write(ENVELOPE_TAG_CLOSE);
    out.flush();
  }

}

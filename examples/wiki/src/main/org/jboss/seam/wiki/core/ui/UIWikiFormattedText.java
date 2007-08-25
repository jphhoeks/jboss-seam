/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.core.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.Component;
import org.jboss.seam.ui.util.JSF;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.wiki.core.engine.WikiLink;
import org.jboss.seam.wiki.core.engine.WikiTextParser;
import org.jboss.seam.wiki.core.engine.WikiTextRenderer;
import org.jboss.seam.wiki.core.engine.WikiLinkResolver;
import org.jboss.seam.wiki.core.model.File;
import org.jboss.seam.wiki.core.model.Document;
import org.jboss.seam.wiki.core.model.Node;
import org.jboss.seam.wiki.util.WikiUtil;

public class UIWikiFormattedText extends UIOutput {

    public static final String ATTR_LINK_STYLE_CLASS                = "linkStyleClass";
    public static final String ATTR_BROKEN_LINK_STYLE_CLASS         = "brokenLinkStyleClass";
    public static final String ATTR_ATTACHMENT_LINK_STYLE_CLASS     = "attachmentLinkStyleClass";
    public static final String ATTR_THUMBNAIL_LINK_STYLE_CLASS      = "thumbnailLinkStyleClass";
    public static final String ATTR_UPDATE_RESOLVED_LINKS           = "updateResolvedLinks";
    public static final String ATTR_RENDER_BASE_DOCUMENT            = "renderBaseDocument";
    public static final String ATTR_RENDER_BASE_DIRECTORY           = "renderBaseDirectory";
    public static final String ATTR_ENABLE_PLUGINS                  = "enablePlugins";

    private List<String> plugins;

    public static final String COMPONENT_FAMILY = "org.jboss.seam.wiki.core.ui.UIWikiFormattedText";

    public static final String COMPONENT_TYPE = "org.jboss.seam.wiki.core.ui.UIWikiFormattedText";

    public UIWikiFormattedText() {
        super();
        plugins = new ArrayList<String>();
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public void encodeBegin(FacesContext facesContext) throws IOException {
        if (!isRendered() || getValue() == null) return;

        // Use the WikiTextParser to resolve macros
        WikiTextParser parser = new WikiTextParser((String) getValue(), false, true);

        // Resolve the base document and directory we are resolving against
        final Document baseDocument = (Document)getAttributes().get(ATTR_RENDER_BASE_DOCUMENT);
        final Node baseDirectory = (Node)getAttributes().get(ATTR_RENDER_BASE_DIRECTORY);
        parser.setCurrentDocument(baseDocument);
        parser.setCurrentDirectory(baseDirectory);

        parser.setResolver((WikiLinkResolver)Component.getInstance("wikiLinkResolver"));

        // Set a customized renderer for parser macro callbacks
        parser.setRenderer(new WikiTextRenderer() {

            public String renderInlineLink(WikiLink inlineLink) {
                return "<a href=\""
                        + (inlineLink.isBroken() ? inlineLink.getUrl() : WikiUtil.renderURL(inlineLink.getNode()))
                        + "\" class=\""
                        + (inlineLink.isBroken() ? getAttributes().get(ATTR_BROKEN_LINK_STYLE_CLASS)
                        : getAttributes().get(ATTR_LINK_STYLE_CLASS)) + "\">"
                        + inlineLink.getDescription() + "</a>";
            }

            public String renderExternalLink(WikiLink externalLink) {
                return "<a href=\""
                        + WikiUtil.escapeEmailURL(externalLink.getUrl())
                        + "\" class=\""
                        + (externalLink.isBroken() ? getAttributes().get(ATTR_BROKEN_LINK_STYLE_CLASS)
                        : getAttributes().get(ATTR_LINK_STYLE_CLASS)) + "\">"
                        + WikiUtil.escapeEmailURL(externalLink.getDescription()) + "</a>";
            }

            public String renderFileAttachmentLink(int attachmentNumber, WikiLink attachmentLink) {
                return "<a href=\""
                        + WikiUtil.renderURL(baseDocument)
                        + "#attachment" + attachmentNumber + "\" class=\""
                        + getAttributes().get(ATTR_ATTACHMENT_LINK_STYLE_CLASS) + "\">"
                        + attachmentLink.getDescription() + "[" + attachmentNumber + "]" + "</a>";
            }

            public String renderThumbnailImageInlineLink(WikiLink inlineLink) {
                File file = (File) inlineLink.getNode();

                if (file.getImageMetaInfo().getThumbnail() == 'F') {
                    // Full size display, no thumbnail

                    String imageUrl = WikiUtil.renderURL(inlineLink.getNode()) + "&amp;cid=" + Conversation.instance().getId();
                    return "<img src='"+ imageUrl + "'" +
                            " width='"+ file.getImageMetaInfo().getSizeX()+"'" +
                            " height='"+ file.getImageMetaInfo().getSizeY() +"'/>";
                } else {
                    // Thumbnail with link display

                    // I have no idea why this needs HTML entities for the & symbol -
                    // Firefox complains about invalid XML if an & is in an attribute
                    // value!
                    String thumbnailUrl = WikiUtil.renderURL(inlineLink.getNode()) + "&amp;thumbnail=true&amp;cid=" + Conversation.instance().getId();

                    return "<a href=\""
                            + (inlineLink.isBroken() ? inlineLink.getUrl() : WikiUtil.renderURL(inlineLink
                            .getNode())) + "\" class=\""
                            + getAttributes().get(ATTR_THUMBNAIL_LINK_STYLE_CLASS) + "\"><img src=\""
                            + thumbnailUrl + "\"/></a>";

                }
            }

            public String renderMacro(String macroName) {
                if (macroName == null || macroName.length() == 0) return "";
                try {
                    new Integer(macroName);
                } catch (NumberFormatException ex) {
                    // This is the name of a not-found plugin, otherwise
                    // we'd have a numeric client identifier of the
                    // included plugin component
                    return "";
                }
                UIComponent child = findComponent(plugins.get(new Integer(macroName)));
                ResponseWriter originalResponseWriter = getFacesContext().getResponseWriter();
                StringWriter stringWriter = new StringWriter();
                ResponseWriter tempResponseWriter = originalResponseWriter
                        .cloneWithWriter(stringWriter);
                getFacesContext().setResponseWriter(tempResponseWriter);
                try {
                    JSF.renderChild(getFacesContext(), child);
                }
                catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                finally {
                    getFacesContext().setResponseWriter(originalResponseWriter);
                }
                return stringWriter.getBuffer().toString();
            }

            public void setAttachmentLinks(List<WikiLink> attachmentLinks) {
                // Put attachments (wiki links...) into the event context for later rendering
                Contexts.getEventContext().set("wikiTextAttachments", attachmentLinks);
            }

            public void setExternalLinks(List<WikiLink> externalLinks) {
                // Put external links (to targets not on this wiki) into the event context for later rendering
                Contexts.getEventContext().set("wikiTextExternalLinks", externalLinks);
            }
        });

        // Run the parser (default to true for updating resolved links)
        Boolean updateResolvedLinks =
                getAttributes().get(ATTR_UPDATE_RESOLVED_LINKS) == null
                || Boolean.valueOf((String) getAttributes().get(ATTR_UPDATE_RESOLVED_LINKS));
        parser.parse(updateResolvedLinks);

        facesContext.getResponseWriter().write(parser.toString());

    }

    protected String addPlugin(String clientId) {
        plugins.add(clientId);
        return (plugins.size() - 1) + "";
    }

}

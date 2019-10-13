package org.jboss.seam.ui.graphicImage;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.jboss.seam.cache.CacheProvider;
import org.jboss.seam.ui.graphicImage.GraphicImageStore.ImageWrapper;
import org.jboss.seam.ui.util.HTML;
import org.jboss.seam.ui.util.cdk.RendererBase;
import org.richfaces.cdk.annotations.JsfRenderer;

/**
 * Renderer for image based components
 * 
 * @author mnovotny
 *
 */
@JsfRenderer(type = "org.jboss.seam.ui.graphicImage.GraphicImageRenderer", family = "org.jboss.seam.ui.GraphicImageRenderer")
public class GraphicImageRendererBase extends RendererBase {

	@Override
	protected Class getComponentClass() {
		return UIGraphicImage.class;
	}

	@Override
	protected void doEncodeBegin(ResponseWriter writer, FacesContext context, UIComponent component) throws IOException {
		UIGraphicImage graphicImage = (UIGraphicImage) component;

		CacheProvider cacheProvider = CacheProvider.instance();

		String key = graphicImage.getFileName();
		String extension = null;

		ImageWrapper wrapper = null;

		Image image = Image.instance();

		if (graphicImage.isInvalidate())
			cacheProvider.remove(graphicImage.getCacheKey());

		if (graphicImage.isCache() && cacheProvider.get(graphicImage.getCacheKey()) != null) {
			wrapper = (ImageWrapper) cacheProvider.get(graphicImage.getCacheKey());
		} else {
			Object value = graphicImage.getValue();
			if (isNullOrEmptyArray(value)) {
				return;
			}

			image.setInput(value);

			// Do transforms

			for (UIComponent cmp : graphicImage.getChildren()) {
				if (cmp instanceof ImageTransform) {
					ImageTransform imageTransform = (ImageTransform) cmp;
					imageTransform.applyTransform(image);
				}
			}

			wrapper = new ImageWrapper(image.getImage(), image.getContentType());

			if (graphicImage.isCache())
				cacheProvider.put(graphicImage.getCacheKey(), wrapper);
		}

		key = GraphicImageStore.instance().put(wrapper, key);
		extension = image.getContentType().getExtension();

		writer.startElement(HTML.IMG_ELEM, graphicImage);
		if (graphicImage.getId() != null) {
			writer.writeAttribute(HTML.ID_ATTR, graphicImage.getClientId(context), HTML.ID_ATTR);
		}

		String url = context.getExternalContext().getRequestContextPath() + GraphicImageResource.GRAPHIC_IMAGE_RESOURCE_PATH + "/" + key
				+ extension;
		writer.writeAttribute(HTML.SRC_ATTR, url, HTML.SRC_ATTR);
		HTML.renderHTMLAttributes(writer, component, HTML.IMG_PASSTHROUGH_ATTRIBUTES);
		writer.endElement(HTML.IMG_ELEM);
	}

	private boolean isNullOrEmptyArray(Object input) {
		if (input == null) {
			return true;
		}
		if (input.getClass().isArray() && input.getClass().getComponentType().isAssignableFrom(Byte.TYPE)) {
			byte[] b = (byte[]) input;
			return b.length == 0;
		}
		return false;
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}

}

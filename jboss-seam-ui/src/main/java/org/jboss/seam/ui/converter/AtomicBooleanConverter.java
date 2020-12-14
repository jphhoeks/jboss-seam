package org.jboss.seam.ui.converter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import org.jboss.seam.util.Strings;

/**
 * Converter for java.util.concurrent.atomic.AtomicBoolean 
 * @author Dennis Byrne
 */
@FacesConverter(value = "org.jboss.seam.ui.AtomicBooleanConverter")
public class AtomicBooleanConverter implements Converter {
	
	public AtomicBooleanConverter() {
		super();
	}

	@Override
	public Object getAsObject(FacesContext ctx, UIComponent ui, String string) {
		if (Strings.isEmpty(string)) {
			return null;
		}
		return new AtomicBoolean(Boolean.parseBoolean(string.trim()));
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent ui, Object object) {
		String string = "";
		if (object != null) {
			if (object instanceof String) {
				string = (String) object;
			} else if (object instanceof AtomicBoolean) {
				string = ((AtomicBoolean) object).toString();
			} else {
				throw new ConverterException("Received an instance of " + object.getClass().getName()
						+ ", but was expecting an instance of " + AtomicInteger.class.getName());
			}
		}
		return string;
	}

}

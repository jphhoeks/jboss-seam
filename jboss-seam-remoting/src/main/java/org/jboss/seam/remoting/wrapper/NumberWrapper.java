package org.jboss.seam.remoting.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.util.CollectionsUtils;

/**
 * Int wrapper class.
 *
 * @author Shane Bryzak
 */
public class NumberWrapper extends BaseWrapper implements Wrapper {
	private static final byte[] NUMBER_TAG_OPEN = "<number>".getBytes();
	private static final byte[] NUMBER_TAG_CLOSE = "</number>".getBytes();

	private Set<Class> exactConversions = CollectionsUtils.unmodifiableSet(
		Integer.class,
		Integer.TYPE,
		Long.class,
		Long.TYPE,
		Short.class,
		Short.TYPE,
		Double.class,
		Double.TYPE,
		Float.class,
		Float.TYPE,
		Byte.class,
		Byte.TYPE			
	);
	
	private Set<Class> compatibleConversions = new HashSet<>(Arrays.asList(
			String.class,
			Object.class
	));
	
	public NumberWrapper() {
		super();
	}
	
	@Override
	public Object convert(Type type) throws ConversionException {
		String val = element.getStringValue().trim();

		if (type.equals(Short.class)) {
			value = !"".equals(val) ? Short.valueOf(val) : null;
		} else if (type.equals(Short.TYPE)) {
			value = Short.valueOf(val);
		} else if (type.equals(Integer.class)) {
			value = !"".equals(val) ? Integer.valueOf(val) : null;
		} else if (type.equals(Integer.TYPE)) {
			value = Integer.valueOf(val);
		} else if (type.equals(Long.class) || type.equals(Object.class)) {
			value = !"".equals(val) ? Long.valueOf(val) : null;
		} else if (type.equals(Long.TYPE)) {
			value = Long.valueOf(val);
		} else if (type.equals(Float.class)) {
			value = !"".equals(val) ? Float.valueOf(val) : null;
		} else if (type.equals(Float.TYPE)) {
			value = Float.parseFloat(val);
		} else if (type.equals(Double.class)) {
			value = !"".equals(val) ? Double.valueOf(val) : null;
		} else if (type.equals(Double.TYPE)) {
			value = Double.valueOf(val);
		} else if (type.equals(Byte.class)) {
			value = !"".equals(val) ? Byte.valueOf(val) : null;
		} else if (type.equals(Byte.TYPE)) {
			value = Byte.valueOf(val);
		} else if (type.equals(String.class)) {
			value = val;
		} else {
			throw new ConversionException(String.format("Value [%s] cannot be converted to type [%s].", element.getStringValue(), type));
		}

		return value;
	}

	/**
	* 
	* @param out OutputStream
	* @throws IOException
	*/
	@Override
	public void marshal(OutputStream out) throws IOException {
		out.write(NUMBER_TAG_OPEN);
		out.write(value.toString().getBytes());
		out.write(NUMBER_TAG_CLOSE);
	}

	/**
	* Allow conversions to either Integer or String.
	* 
	* @param cls Class
	* @return ConversionScore
	*/
	@Override
	public ConversionScore conversionScore(Class cls) {
		if (cls == null) {
			return ConversionScore.nomatch;
		}
		
		if (exactConversions.contains(cls)) {
			return ConversionScore.exact;
		}		

		if (compatibleConversions.contains(cls)) {
			return ConversionScore.compatible;
		}

		return ConversionScore.nomatch;
	}
}

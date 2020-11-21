package org.jboss.seam.remoting.wrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.jboss.seam.util.CollectionsUtils;

/**
 * @author Shane Bryzak
 */
public class MapWrapper extends BaseWrapper implements Wrapper {
	private static final byte[] MAP_TAG_OPEN = "<map>".getBytes();
	private static final byte[] MAP_TAG_CLOSE = "</map>".getBytes();

	private static final byte[] ELEMENT_TAG_OPEN = "<element>".getBytes();
	private static final byte[] ELEMENT_TAG_CLOSE = "</element>".getBytes();

	private static final byte[] KEY_TAG_OPEN = "<k>".getBytes();
	private static final byte[] KEY_TAG_CLOSE = "</k>".getBytes();

	private static final byte[] VALUE_TAG_OPEN = "<v>".getBytes();
	private static final byte[] VALUE_TAG_CLOSE = "</v>".getBytes();

	@Override
	public void marshal(OutputStream out) throws IOException {
		out.write(MAP_TAG_OPEN);

		Map<Object, Object> m = (Map) this.value;

		for (Map.Entry<Object, Object> entry: m.entrySet()) {
			
			Object key = entry.getKey();
			Object value = entry.getValue();
			
			out.write(ELEMENT_TAG_OPEN);

			out.write(KEY_TAG_OPEN);
			context.createWrapperFromObject(key, String.format("%s[key]", path)).marshal(out);
			out.write(KEY_TAG_CLOSE);

			out.write(VALUE_TAG_OPEN);
			context.createWrapperFromObject(value, String.format("%s[value]", path)).marshal(out);
			out.write(VALUE_TAG_CLOSE);

			out.write(ELEMENT_TAG_CLOSE);
		}

		out.write(MAP_TAG_CLOSE);
	}

	@Override
	public Object convert(Type type) throws ConversionException {
		if (context == null) {
			throw new IllegalStateException("No call context has been set");
		}

		Class typeClass = null;
		Type keyType = null;
		Type valueType = null;

		// Either the type should be a generified Map
		if (type instanceof ParameterizedType && Map.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType())) {
			typeClass = (Class) ((ParameterizedType) type).getRawType();

			for (Type t : ((ParameterizedType) type).getActualTypeArguments()) {
				if (keyType == null) {
					keyType = t;
				} else {
					valueType = t;
					break;
				}
			}
		}
		// Or a non-generified Map
		else if (type instanceof Class && Map.class.isAssignableFrom((Class) type)) {
			if (!((Class) type).isInterface()) {
				typeClass = (Class) type;
			}
			keyType = Object.class;
			valueType = Object.class;
		}
		// If it's neither, throw an exception
 else {
			throw new ConversionException(String.format("Cannot convert value to type [%s]", type));
		}

		List<Element> elements = element.elements("element");
		// If we don't have a concrete type, default to creating a HashMap
		if (typeClass == null || typeClass.isInterface()) {
			value = CollectionsUtils.newHashMap(elements.size());
		} else {
			try {
				// Otherwise create an instance of the concrete type
				if (type instanceof Class) {
					value = ((Class) type).getDeclaredConstructor().newInstance();
				} else if (type instanceof ParameterizedType) {
					value = ((Class) ((ParameterizedType) type).getRawType()).getDeclaredConstructor().newInstance();
				}
			} catch (Exception ex) {
				throw new ConversionException(String.format("Could not create value of type [%s]", type), ex);
			}
		}

		for (Element e : elements) {
			Element keyElement = e.element("k").elementIterator().next();
			Element valueElement = e.element("v").elementIterator().next();

			((Map) value).put(context.createWrapperFromElement(keyElement).convert(keyType),
					context.createWrapperFromElement(valueElement).convert(valueType));
		}

		return value;
	}

	@Override
	public ConversionScore conversionScore(Class cls) {
		if (Map.class.isAssignableFrom(cls)) {
			return ConversionScore.exact;
		}

		if (cls.equals(Object.class)) {
			return ConversionScore.compatible;
		}

		return ConversionScore.nomatch;
	}
}

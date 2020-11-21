package org.jboss.seam.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class Conversions {
	private static final String EXPRESSION_MARKER = "#{";
	private static final char EXPRESSION_ESCAPE_CHAR = '\\';

	private static Map<Class, Converter> converters = new ConcurrentHashMap<Class, Converter>();
	
	static {
		converters.put(String.class, new StringConverter());
		converters.put(Boolean.class, new BooleanConverter());
		converters.put(boolean.class, new BooleanConverter());
		converters.put(Integer.class, new IntegerConverter());
		converters.put(int.class, new IntegerConverter());
		converters.put(Long.class, new LongConverter());
		converters.put(long.class, new LongConverter());
		converters.put(Float.class, new FloatConverter());
		converters.put(float.class, new FloatConverter());
		converters.put(Double.class, new DoubleConverter());
		converters.put(double.class, new DoubleConverter());
		converters.put(Character.class, new CharacterConverter());
		converters.put(char.class, new CharacterConverter());
		converters.put(String[].class, new StringArrayConverter());
		converters.put(Set.class, new SetConverter());
		converters.put(List.class, new ListConverter());
		converters.put(Map.class, new MapConverter());
		converters.put(Properties.class, new PropertiesConverter());
		//converters.put(Date.class, new DateTimeConverter());
		//converters.put(Short.class, new ShortConverter());
		//converters.put(Byte.class, new ByteConverter());
		converters.put(Enum.class, new EnumConverter());
		converters.put(BigInteger.class, new BigIntegerConverter());
		converters.put(BigDecimal.class, new BigDecimalConverter());
		converters.put(Class.class, new ClassConverter());
	}

	public static <Y> void putConverter(Class<Y> type, Converter<Y> converter) {
		converters.put(type, converter);
	}

	public static <Y> Converter<Y> getConverter(Class<Y> clazz) {
		Converter<Y> converter = converters.get(clazz);
		if (converter == null && clazz != null && clazz.isEnum()) {
			converter = converters.get(Enum.class);
		}

		if (converter == null) {
			throw new IllegalArgumentException("No converter for type: " + clazz.getName());
		}
		return converter;
	}

	public static interface Converter<Z> {
		public Z toObject(PropertyValue value, Type type);
	}

	public static class BooleanConverter implements Converter<Boolean> {
		@Override
		public Boolean toObject(PropertyValue value, Type type) {
			return Boolean.valueOf(value.getSingleValue());
		}
	}

	public static class IntegerConverter implements Converter<Integer> {
		@Override
		public Integer toObject(PropertyValue value, Type type) {
			return Integer.valueOf(value.getSingleValue());
		}
	}

	public static class LongConverter implements Converter<Long> {
		@Override
		public Long toObject(PropertyValue value, Type type) {
			return Long.valueOf(value.getSingleValue());
		}
	}

	public static class FloatConverter implements Converter<Float> {
		@Override
		public Float toObject(PropertyValue value, Type type) {
			return Float.valueOf(value.getSingleValue());
		}
	}

	public static class DoubleConverter implements Converter<Double> {
		@Override
		public Double toObject(PropertyValue value, Type type) {
			return Double.valueOf(value.getSingleValue());
		}
	}

	public static class CharacterConverter implements Converter<Character> {
		@Override
		public Character toObject(PropertyValue value, Type type) {
			return value.getSingleValue().charAt(0);
		}
	}

	public static class StringConverter implements Converter<String> {
		@Override
		public String toObject(PropertyValue value, Type type) {
			return value.getSingleValue();
		}
	}

	public static class BigDecimalConverter implements Converter<BigDecimal> {
		@Override
		public BigDecimal toObject(PropertyValue value, Type type) {
			return new BigDecimal(value.getSingleValue());
		}
	}

	public static class BigIntegerConverter implements Converter<BigInteger> {
		@Override
		public BigInteger toObject(PropertyValue value, Type type) {
			return new BigInteger(value.getSingleValue());
		}
	}

	public static class EnumConverter implements Converter<Enum<?>> {
		@Override
		public Enum<?> toObject(PropertyValue value, Type type) {
			return Enum.valueOf((Class<Enum>) type, value.getSingleValue());
		}
	}

	public static class StringArrayConverter implements Converter<String[]> {
		@Override
		public String[] toObject(PropertyValue values, Type type) {
			return values.getMultiValues();
		}
	}

	public static class ArrayConverter implements Converter {
		@Override
		public Object toObject(PropertyValue values, Type type) {
			String[] strings = values.getMultiValues();
			Class elementType = ((Class) type).getComponentType();
			Object objects = Array.newInstance(elementType, strings.length);
			Converter elementConverter = converters.get(elementType);
			for (int i = 0; i < strings.length; i++) {
				Object element = elementConverter.toObject(new FlatPropertyValue(strings[i]), elementType);
				Array.set(objects, i, element);
			}
			return objects;
		}
	}

	public static class SetConverter implements Converter<Set> {
		@Override
		public Set toObject(PropertyValue values, Type type) {
			String[] strings = values.getMultiValues();
			Class elementType = Reflections.getCollectionElementType(type);
			Set<Object> set = new HashSet<Object>(strings.length);
			Converter elementConverter = converters.get(elementType);
			for (String value : strings) {
				Object element = elementConverter.toObject(new FlatPropertyValue(value), elementType);
				set.add(element);
			}
			return set;
		}
	}

	public static class ListConverter implements Converter<List> {
		@Override
		public List toObject(PropertyValue values, Type type) {
			String[] strings = values.getMultiValues();
			Class elementType = Reflections.getCollectionElementType(type);
			List<Object> list = new ArrayList<Object>(strings.length);
			Converter elementConverter = converters.get(elementType);
			for (String value: strings) {
				list.add(getElementValue(elementType, elementConverter, value));
			}
			return list;
		}

	}

	private static Object getElementValue(Class elementType, Converter elementConverter, String string) {
		PropertyValue propertyValue = new FlatPropertyValue(string);
		if (propertyValue.isExpression()) {
			throw new IllegalArgumentException("No expressions allowed here");
		}
		if (elementConverter == null) {
			throw new IllegalArgumentException("No converter for element type: " + elementType.getName());
		}
		return elementConverter.toObject(propertyValue, elementType);
	}

	public static class MapConverter implements Converter<Map> {
		@Override
		public Map toObject(PropertyValue values, Type type) {
			Map<String, String> keyedValues = values.getKeyedValues();
			Class elementType = Reflections.getCollectionElementType(type);
			Map map = new HashMap(keyedValues.size());
			Converter elementConverter = converters.get(elementType);
			for (Map.Entry<String, String> me : keyedValues.entrySet()) {
				map.put(me.getKey(), getElementValue(elementType, elementConverter, me.getValue()));
			}
			return map;
		}
	}

	public static class PropertiesConverter implements Converter<Properties> {
		@Override
		public Properties toObject(PropertyValue values, Type type) {
			Map<String, String> keyedValues = values.getKeyedValues();
			Properties map = new Properties();
			Converter elementConverter = converters.get(String.class);
			for (Map.Entry<String, String> me : keyedValues.entrySet()) {
				String key = me.getKey();
				Object element = elementConverter.toObject(new FlatPropertyValue(me.getValue()), String.class);
				map.put(key, element);
			}
			return map;
		}
	}

	public static class ClassConverter implements Converter<Class> {
		@Override
		public Class toObject(PropertyValue value, Type type) {
			try {
				return Reflections.classForName(value.getSingleValue());
			} catch (ClassNotFoundException cnfe) {
				throw new IllegalArgumentException(cnfe);
			}
		}
	}

	public static interface PropertyValue extends Serializable {
		Map<String, String> getKeyedValues();

		String[] getMultiValues();

		String getSingleValue();

		boolean isExpression();

		boolean isMultiValued();

		boolean isAssociativeValued();

		Class getType();
	}

	public static class FlatPropertyValue implements PropertyValue {

		private String string;

		public FlatPropertyValue(String string) {
			if (string == null) {
				throw new IllegalArgumentException("null value");
			}
			this.string = string;
		}

		@Override
		public String[] getMultiValues() {
			return Strings.split(string, ", \r\n\f\t");
		}

		@Override
		public String getSingleValue() {
			return string;
		}

		@Override
		public boolean isExpression() {
			boolean containsExpr = false;
			int idx = string.indexOf(EXPRESSION_MARKER);
			if (idx == 0) {
				containsExpr = true;
			} else {
				while (idx != -1) {
					if (string.charAt(idx - 1) == EXPRESSION_ESCAPE_CHAR) {
						idx = string.indexOf(EXPRESSION_MARKER, idx + 2);
					} else {
						containsExpr = true;
						break;
					}
				}
			}
			return containsExpr;
		}

		@Override
		public boolean isMultiValued() {
			return false;
		}

		@Override
		public boolean isAssociativeValued() {
			return false;
		}

		@Override
		public Map<String, String> getKeyedValues() {
			throw new UnsupportedOperationException("not a keyed property value");
		}

		@Override
		public String toString() {
			return string;
		}

		@Override
		public Class getType() {
			return null;
		}

	}

	public static class MultiPropertyValue implements PropertyValue {
		private String[] strings;

		private Class type;

		public MultiPropertyValue(String[] strings, Class type) {
			if (strings == null) {
				throw new IllegalArgumentException();
			}
			this.strings = CloneUtils.cloneArray(strings);
			this.type = type;
		}

		@Override
		public String[] getMultiValues() {
			return CloneUtils.cloneArray(strings);
		}

		@Override
		public String getSingleValue() {
			throw new UnsupportedOperationException("not a flat property value");
		}

		@Override
		public Map<String, String> getKeyedValues() {
			throw new UnsupportedOperationException("not a keyed property value");
		}

		@Override
		public boolean isMultiValued() {
			return true;
		}

		@Override
		public boolean isAssociativeValued() {
			return false;
		}

		@Override
		public boolean isExpression() {
			return false;
		}

		@Override
		public String toString() {
			return Strings.toString(", ", (Object[]) strings);
		}

		@Override
		public Class getType() {
			return type;
		}
	}

	public static class AssociativePropertyValue implements PropertyValue {
		private Map<String, String> keyedValues;

		private Class type;

		public AssociativePropertyValue(Map<String, String> keyedValues, Class type) {
			if (keyedValues == null) {
				throw new IllegalArgumentException();
			}
			this.keyedValues = keyedValues;
			this.type = type;
		}

		@Override
		public String[] getMultiValues() {
			throw new UnsupportedOperationException("not a multi-valued property value");
		}

		@Override
		public String getSingleValue() {
			throw new UnsupportedOperationException("not a flat property value");
		}

		@Override
		public Map<String, String> getKeyedValues() {
			return keyedValues;
		}

		@Override
		public boolean isExpression() {
			return false;
		}

		@Override
		public boolean isMultiValued() {
			return false;
		}

		@Override
		public boolean isAssociativeValued() {
			return true;
		}

		@Override
		public String toString() {
			return keyedValues.toString();
		}

		@Override
		public Class getType() {
			return type;
		}

	}

}

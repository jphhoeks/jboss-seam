package org.jboss.seam.wicket.ioc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.jboss.seam.util.Reflections;

public class InjectedField<T extends Annotation> implements InjectedAttribute<T> {

	protected Field field;
	protected T annotation;

	public InjectedField(Field field, T annotation) {
		this.field = field;
		this.annotation = annotation;
	}

	@Override
	public Field getMember() {
		return field;
	}

	@Override
	public T getAnnotation() {
		return annotation;
	}

	@Override
	public Class getType() {
		return field.getType();
	}

	@Override
	public void set(Object bean, Object value) {
		field.setAccessible(true);
		Reflections.setAndWrap(field, bean, value);
	}

	@Override
	public String toString() {
		return "InjectedField(" + Reflections.toString(field) + ')';
	}

}
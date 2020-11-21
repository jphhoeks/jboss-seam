package org.jboss.seam.jsf;


import java.util.Map;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ApplicationWrapper;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import javax.servlet.ServletContext;

import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.el.SeamExpressionFactory;

/**
 * Proxies the JSF Application object, and adds all kinds
 * of tasty extras.
 *
 * Extending javax.faces.application.ApplicationWrapper
 * so all JSF 2.2+ functions is available without needing to change this class
 *
 * @author Gavin King
 * @author William Dutton
 *
 */
public class SeamApplication extends ApplicationWrapper {

	protected Application application;

	public SeamApplication(Application application) {
		super();
		this.application = application;
	}

	public Application getDelegate() {
		return application;
	}

	@Override
	public Application getWrapped() {
		return application;
	}

	/**
	 * <p class="changed_added_2_0">The default behavior of this method
	 * is to call {@link Application#createConverter(String)} on the
	 * wrapped {@link Application} object.
	 * IF ApplicationContext is NOT Active and converterId is NULL
	 * else return Component.getInstance by id</p>
	 */
	@Override
	public Converter createConverter(String converterId) {
		if ( Contexts.isApplicationContextActive() ) {
			String name = Init.instance().getConverters().get(converterId);
			if (name!=null) {
				return (Converter) Component.getInstance(name);
			}
		}
		return application.createConverter(converterId);
	}

	@Override
	public Converter createConverter(Class<?> targetClass) {
		Converter converter = null;
		if ( Contexts.isApplicationContextActive() ) {
			converter = new ConverterLocator(targetClass).getConverter();
		}
		if (converter == null) {
			converter = application.createConverter(targetClass);
		}
		return converter;
	}

	private static class ConverterLocator {

		private Map<Class<?>, String> converters;
		private Class<?> targetClass;
		private Converter converter;

		public ConverterLocator(Class<?> targetClass) {
			converters = Init.instance().getConvertersByClass();
			this.targetClass = targetClass;
		}

		public Converter getConverter() {
			if (converter == null) {
				locateConverter(targetClass);
			}
			return converter;
		}

		private Converter createConverter(Class<?> clazz) {
			return (Converter) Component.getInstance(converters.get(clazz));
		}

		private void locateConverter(Class<?> clazz) {
			if (converters.containsKey(clazz)) {
				converter = createConverter(clazz);
				return;
			}

			for (Class<?> _interface: clazz.getInterfaces()) {
				if (converters.containsKey(_interface)) {
					converter = createConverter(_interface);
					return;
				} else {
					locateConverter(_interface);
					if (converter != null) {
						return;
					}
				}
			}

			Class<?> superClass = clazz.getSuperclass();
			if (converters.containsKey(superClass)) {
				converter = createConverter(superClass);
				return;
			} else if (superClass != null) {
				locateConverter(superClass);
			}
		}
	}

	@Override
	public Validator createValidator(String validatorId) throws FacesException {
		if ( Contexts.isApplicationContextActive() ) {
			String name = Init.instance().getValidators().get(validatorId);
			if (name!=null) {
				return (Validator) Component.getInstance(name);
			}
		}
		return application.createValidator(validatorId);
	}

	/**
	 * Overrides default behavior of jsf 2.2 api spec
	 * by using SEAM version instead of JSF Application version.
	 */
	@Override
	public ExpressionFactory getExpressionFactory() {
		//JBoss EL
		return SeamExpressionFactory.INSTANCE;
	}

	@Override
	public String toString() {
		return application.toString();
	}

	public static boolean isSeamApplication (FacesContext facesContext) {
		ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
		Object attributeVersion = servletContext.getAttribute(Seam.VERSION);
		return attributeVersion != null;
	}
}

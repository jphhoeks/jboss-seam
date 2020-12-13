package org.jboss.seam.deployment;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.CollectionsUtils;

public class AnnotationDeploymentHandler extends AbstractClassDeploymentHandler {
	/**
	* Name under which this {@link DeploymentHandler} is registered
	*/
	public static final String NAME = "org.jboss.seam.deployment.AnnotationDeploymentHandler";

	public static final String ANNOTATIONS_KEY = "org.jboss.seam.deployment.annotationTypes";

	private static final LogProvider log = Logging.getLogProvider(AnnotationDeploymentHandler.class);

	private ClassDeploymentMetadata metadata;

	private Map<String, Set<Class<?>>> classes;
	private Set<Class<? extends Annotation>> annotations;
	
	private static class AnnotationDeploymentHandlerMetadata implements ClassDeploymentMetadata {

		private Set<Class<? extends Annotation>> annotations;

		public AnnotationDeploymentHandlerMetadata(Set<Class<? extends Annotation>> annotations) {
			super();
			this.annotations = annotations;
		}

		@Override
		public Set<Class<? extends Annotation>> getClassAnnotatedWith() {
			return annotations;
		}

		@Override
		public String getFileNameSuffix() {
			return null;
		}

	}



	public AnnotationDeploymentHandler(List<String> annotationTypes, ClassLoader classLoader) {
		super();
		this.annotations =  CollectionsUtils.newHashSet(annotationTypes.size());
		for (String classname : annotationTypes) {
			try {
				this.annotations.add((Class<? extends Annotation>) classLoader.loadClass(classname));
			} catch (ClassNotFoundException cnfe) {
				if (log.isWarnEnabled()) { 
					log.warn("could not load annotation class: " + classname, cnfe);
				}
			} catch (LinkageError ncdfe) {
				if (log.isWarnEnabled()) { 
					log.warn("could not load annotation class (missing dependency): " + classname, ncdfe);
				}
			} catch (ClassCastException cce) {
				if (log.isWarnEnabled()) { 
					log.warn("could not load annotation class (not an annotation): " + classname, cce);
				}
			}
		}
		metadata = new AnnotationDeploymentHandlerMetadata(this.annotations);
	}

	/**
	* Get annotated classes
	*/
	public Map<String, Set<Class<?>>> getClassMap() {
		return Collections.unmodifiableMap(classes);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public ClassDeploymentMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void postProcess(ClassLoader classLoader) {
		classes = CollectionsUtils.newHashMap(annotations.size());
		for (Class<? extends Annotation> annotationType : annotations) {
			classes.put(annotationType.getName(), new HashSet<Class<?>>());
		}
		for (ClassDescriptor classDescriptor : getClasses()) {
			for (Annotation annotation : classDescriptor.getClazz().getAnnotations()) {
				if (classes.containsKey(annotation.annotationType().getName())) {
					classes.get(annotation.annotationType().getName()).add(classDescriptor.getClazz());
				}
			}
		}
	}

}

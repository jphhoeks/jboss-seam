package org.jboss.seam.deployment;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * The standard deployment strategy used with Seam, deploys non-hot-deployable
 * Seam components and namespaces
 * 
 * @author Pete Muir
 *
 */
public class StandardDeploymentStrategy extends DeploymentStrategy {

	private ClassLoader classLoader;

	private ServletContext servletContext;

	/**
	* The files used to identify a Seam archive
	*/
	public static final String[] RESOURCE_NAMES = { "seam.properties", "META-INF/seam.properties", "META-INF/components.xml" };

	/**
	* The contextual variable name this deployment strategy is made available at
	* during Seam startup.
	*/
	public static final String NAME = "deploymentStrategy";

	/**
	* The key under which to list extra deployment handlers.
	* 
	* This can be specified as a System property or in 
	* /META-INF/seam-deployment.properties
	*/
	public static final String HANDLERS_KEY = "org.jboss.seam.deployment.deploymentHandlers";

	private ComponentDeploymentHandler componentDeploymentHandler;
	private ComponentsXmlDeploymentHandler componentsXmlDeploymentHandler;
	private NamespaceDeploymentHandler namespaceDeploymentHandler;
	private EnumDeploymentHandler enumDeploymentHandler;
	private AnnotationDeploymentHandler annotationDeploymentHandler;
	private DotComponentDotXmlDeploymentHandler dotComponentDotXmlDeploymentHandler;

	private static final LogProvider log = Logging.getLogProvider(StandardDeploymentStrategy.class);

	/**
	* @param classLoader The classloader used to load and handle resources
	*/
	public StandardDeploymentStrategy(ClassLoader classLoader, ServletContext servletContext) {
		super();
		this.classLoader = Thread.currentThread().getContextClassLoader();
		this.servletContext = servletContext;
		componentDeploymentHandler = new ComponentDeploymentHandler();
		getDeploymentHandlers().put(ComponentDeploymentHandler.NAME, componentDeploymentHandler);
		componentsXmlDeploymentHandler = new ComponentsXmlDeploymentHandler();
		getDeploymentHandlers().put(ComponentsXmlDeploymentHandler.NAME, componentsXmlDeploymentHandler);
		dotComponentDotXmlDeploymentHandler = new DotComponentDotXmlDeploymentHandler();
		getDeploymentHandlers().put(DotComponentDotXmlDeploymentHandler.NAME, dotComponentDotXmlDeploymentHandler);
		namespaceDeploymentHandler = new NamespaceDeploymentHandler();
		getDeploymentHandlers().put(NamespaceDeploymentHandler.NAME, namespaceDeploymentHandler);
		enumDeploymentHandler = new EnumDeploymentHandler();
		boolean enableEnums = Boolean.parseBoolean(servletContext.getInitParameter(EnumDeploymentHandler.NAME));
		if (enableEnums) {
			log.info("Enum scanning enabled");
			getDeploymentHandlers().put(EnumDeploymentHandler.NAME, enumDeploymentHandler);
		}
		annotationDeploymentHandler = new AnnotationDeploymentHandler(
				new SeamDeploymentProperties(classLoader).getPropertyValues(AnnotationDeploymentHandler.ANNOTATIONS_KEY), classLoader);
		if (!annotationDeploymentHandler.getMetadata().getClassAnnotatedWith().isEmpty()) {
			getDeploymentHandlers().put(AnnotationDeploymentHandler.NAME, annotationDeploymentHandler);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	protected String getDeploymentHandlersKey() {
		return HANDLERS_KEY;
	}

	/**
	* Get all annotated components known to this strategy
	*/
	public Set<ClassDescriptor> getAnnotatedComponents() {
		return Collections.unmodifiableSet(componentDeploymentHandler.getClasses());
	}

	/**
	* Get all XML defined (throught components.xml and component.xml) components
	*/
	public Set<FileDescriptor> getXmlComponents() {
		Set<FileDescriptor> fileDescriptors = new HashSet<FileDescriptor>();
		fileDescriptors.addAll(componentsXmlDeploymentHandler.getResources());
		fileDescriptors.addAll(dotComponentDotXmlDeploymentHandler.getResources());
		return Collections.unmodifiableSet(fileDescriptors);
	}

	/**
	* Get all scanned and handled Seam namespaces
	*/
	public Set<Package> getScannedNamespaces() {
		return Collections.unmodifiableSet(namespaceDeploymentHandler.getPackages());
	}

	/**
	* Get all scanned and handled enums
	* 
	*/
	public Set<Class<? extends Enum<?>>> getScannedEnums() {
		return Collections.unmodifiableSet(enumDeploymentHandler.getEnums());
	}

	public Map<String, Set<Class<?>>> getAnnotatedClasses() {
		return Collections.unmodifiableMap(annotationDeploymentHandler.getClassMap());
	}

	@Override
	public void scan() {
		log.info("Scanning resources");
		getScanner().scanResources(RESOURCE_NAMES);
		log.info("Scanning directories");
		getScanner().scanDirectories(getFiles().toArray(new File[0]));
		log.info("PostScan");
		postScan();
	}

	public static StandardDeploymentStrategy instance() {
		if (Contexts.getEventContext().isSet(NAME)) {
			return (StandardDeploymentStrategy) Contexts.getEventContext().get(NAME);
		}
		return null;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}
}

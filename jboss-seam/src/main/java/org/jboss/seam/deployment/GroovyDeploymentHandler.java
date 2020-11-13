package org.jboss.seam.deployment;

import groovy.lang.GroovyRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.groovy.control.CompilationFailedException;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;

/**
 * A deployment handler for (uncompiled) Groovy Seam components
 * 
 * @author Pete Muir
 *
 */
public class GroovyDeploymentHandler extends AbstractDeploymentHandler {

	
	private DeploymentMetadata metadata;
	
	private static final LogProvider log = Logging.getLogProvider(GroovyDeploymentHandler.class);
	
	public static final String NAME = "org.jboss.seam.deployment.GroovyDeploymentHandler";
	
	private final String groovyFileExtension;
	
	private Set<ClassDescriptor> classes;

	private class GroovyDeploymentHandlerMetadata implements DeploymentMetadata {

		private String groovyExtension;

		public GroovyDeploymentHandlerMetadata(String groovyExtension) {
			super();
			this.groovyExtension = groovyExtension;
		}

		@Override
		public String getFileNameSuffix() {
			return groovyExtension;
		}

	}

	/**
	* 
	* @param groovyFileExtension The extension to use for the groovy file
	*/
	public GroovyDeploymentHandler(String groovyFileExtension) {
		super();
		this.groovyFileExtension = groovyFileExtension;
		this.classes = new HashSet<ClassDescriptor>();
		this.metadata = new GroovyDeploymentHandlerMetadata(groovyFileExtension);
	}

	/**
	* Get all the Groovy Seam Components this handler has handled 
	*/
	public Set<ClassDescriptor> getClasses() {
		return classes;
	}

	@Override
	public void postProcess(ClassLoader classLoader) {
		for (FileDescriptor fileDescriptor : getResources()) {
			if (log.isDebugEnabled()) { 
				log.debug("Found a groovy file: " + fileDescriptor.getName());
			}
			String classname = filenameToGroovyname(fileDescriptor.getName());
			String filename = groovyComponentFilename(fileDescriptor.getName());
			BufferedReader buffReader = null;
			InputStream stream = null;
			try {
				stream = classLoader.getResourceAsStream(fileDescriptor.getName());
				buffReader = new BufferedReader(new InputStreamReader(stream));
				String line = buffReader.readLine();
				while (line != null) {
					if (line.indexOf("@Name") != -1 || line.indexOf("@" + Name.class.getName()) != -1) {
						//possibly a Seam component
						if (log.isDebugEnabled()) {
							log.debug("Groovy file possibly a Seam component: " + fileDescriptor.getName());
						}
						Class<Object> groovyClass = (Class<Object>) classLoader.loadClass(classname);
						Install install = groovyClass.getAnnotation(Install.class);
						boolean installable = (install == null || install.value())
								&& (groovyClass.isAnnotationPresent(Name.class) || classLoader.getResources(filename).hasMoreElements());
						if (installable) {
							
							if (log.isDebugEnabled()) { 
								log.debug("found groovy component class: " + fileDescriptor.getName());
							}
							classes.add(new ClassDescriptor(fileDescriptor.getName(), fileDescriptor.getUrl(), groovyClass));
						}
						break;
					}
					line = buffReader.readLine();
				}
			} catch (ClassNotFoundException cnfe) {
				if (log.isDebugEnabled()) { 
					log.debug("could not load groovy class: " + classname, cnfe);
				}
			} catch (LinkageError ncdfe) {
				if (log.isDebugEnabled()) { 
					log.debug("could not load groovy class (missing dependency): " + classname, ncdfe);
				}
			} catch (IOException ioe) {
				if (log.isDebugEnabled()) { 
					log.debug("could not load groovy file: " + classname, ioe);
				}
			} catch (CompilationFailedException e) {
				if (log.isDebugEnabled()) { 
					log.debug("Compilation error in Groovy file:" + classname, e);
				}
			} catch (GroovyRuntimeException e) {
				if (log.isDebugEnabled()) { 
					log.debug("Unknown error reading Groovy file:" + classname, e);
				}
			} finally {
				Resources.close(buffReader, stream);
			}
		}
	}

	private String filenameToGroovyname(String filename) {
		return filename.substring(0, filename.lastIndexOf(groovyFileExtension)).replace('/', '.').replace('\\', '.');
	}

	private String groovyComponentFilename(String name) {
		return name.substring(0, name.lastIndexOf(groovyFileExtension)) + ".component.xml";
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public DeploymentMetadata getMetadata() {
		return metadata;
	}

}

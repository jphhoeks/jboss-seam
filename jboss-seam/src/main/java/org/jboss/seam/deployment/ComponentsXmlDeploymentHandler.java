package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The {@link ComponentsXmlDeploymentHandler} components.xml and .component.xml files 
 * 
 * @author Pete Muir
 *
 */
public class ComponentsXmlDeploymentHandler extends AbstractDeploymentHandler implements DeploymentMetadata {

	private final Pattern INF_PATTERN = Pattern.compile("(WEB-INF/components.xml$)|(META-INF/components.xml$)");

	/**
	* Name under which this {@link DeploymentHandler} is registered
	*/
	public static final String NAME = "org.jboss.seam.deployment.ComponentsXmlDeploymentHandler";

	public ComponentsXmlDeploymentHandler() {
		super();
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getFileNameSuffix() {
		return "components.xml";
	}

	@Override
	public DeploymentMetadata getMetadata() {
		return this;
	}

	@Override
	public void postProcess(ClassLoader classLoader) {
		Set<FileDescriptor> resources = new HashSet<FileDescriptor>();
		for (FileDescriptor fileDescriptor : getResources()) {
			// we want to skip over known meta-directories since Seam will auto-load these without a scan
			String path = fileDescriptor.getName();
			if (!this.INF_PATTERN.matcher(path).matches() && !path.contains("/seam-gen/")) {
				resources.add(fileDescriptor);
			}
		}
		setResources(resources);
	}

}

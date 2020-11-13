package org.jboss.seam.deployment;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractClassDeploymentHandler extends AbstractDeploymentHandler implements ClassDeploymentHandler {

	private Set<ClassDescriptor> classes;

	public AbstractClassDeploymentHandler() {
		super();
		classes = new HashSet<ClassDescriptor>();
	}

	@Override
	public Set<ClassDescriptor> getClasses() {
		return classes;
	}

	@Override
	public void setClasses(Set<ClassDescriptor> classes) {
		this.classes = classes;
	}

}

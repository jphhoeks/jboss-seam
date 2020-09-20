package org.jboss.seam.deployment;

import java.io.File;

import javax.servlet.ServletContext;

public abstract class ForwardingAbstractScanner extends AbstractScanner {

	@Deprecated
	public ForwardingAbstractScanner() {
		super();
	}

	public ForwardingAbstractScanner(ServletContext servletContext) {
		super(servletContext);
	}

	protected abstract AbstractScanner delegate();

	@Override
	public DeploymentStrategy getDeploymentStrategy() {
		return delegate().getDeploymentStrategy();
	}

	@Override
	public long getTimestamp() {
		return delegate().getTimestamp();
	}

	@Override
	public void scanDirectories(File[] directories) {
		delegate().scanDirectories(directories);
	}

	@Override
	public void scanResources(String[] resources) {
		delegate().scanResources(resources);
	}

	@Override
	public boolean equals(Object obj) {
		return delegate().equals(obj);
	}

	@Override
	protected boolean handle(String name) {
		return delegate().handle(name);
	}

	@Override
	public int hashCode() {
		return delegate().hashCode();
	}

	@Override
	public String toString() {
		return delegate().toString();
	}

}

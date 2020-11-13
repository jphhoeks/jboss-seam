package org.jboss.seam.bpm;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jboss.seam.deployment.AbstractDeploymentHandler;
import org.jboss.seam.deployment.DeploymentMetadata;
import org.jboss.seam.deployment.FileDescriptor;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Resources;
import org.jboss.seam.util.XML;

public class PageflowDeploymentHandler extends AbstractDeploymentHandler {

	private static DeploymentMetadata NAMESPACE_METADATA = new DeploymentMetadata() {
		@Override
		public String getFileNameSuffix() {
			return ".jpdl.xml";
		}
	};

	private static LogProvider log = Logging.getLogProvider(PageflowDeploymentHandler.class);

	public static final String NAME = "org.jboss.seam.bpm.PageflowDeploymentHandler";

	public PageflowDeploymentHandler() {
		super();
	}
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void postProcess(ClassLoader classLoader) {
		Set<FileDescriptor> files = new HashSet<FileDescriptor>();
		for (FileDescriptor fileDescriptor : getResources()) {
			try {
				InputStream inputStream = null;
				try {
					inputStream = fileDescriptor.getUrl().openStream();
					Element root = XML.getRootElementSafely(inputStream);
					if ("pageflow-definition".equals(root.getName())) {
						files.add(fileDescriptor);
					}
				} catch (DocumentException e) {
					if (log.isDebugEnabled()) {
						log.debug("Unable to parse " + fileDescriptor.getName(), e);
					}
				} finally {
					Resources.close(inputStream);
				}
			} catch (IOException e) {
				if (log.isTraceEnabled()) {
					log.trace("Error loading " + fileDescriptor.getName());
				}
			}

		}
		setResources(files);
	}

	@Override
	public DeploymentMetadata getMetadata() {
		return NAMESPACE_METADATA;
	}

}

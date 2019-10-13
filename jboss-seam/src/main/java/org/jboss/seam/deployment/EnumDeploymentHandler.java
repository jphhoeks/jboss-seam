/*
 * JBoss, Home of Professional Open Source
 * Copyright ${year}, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.seam.deployment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * A deployment handler for enums
 * 
 * @author Stefano Aquino
 * 
 */
public class EnumDeploymentHandler extends AbstractDeploymentHandler {
	private static DeploymentMetadata ENUM_METADATA = new DeploymentMetadata() {
		public String getFileNameSuffix() {
			return ".class";
		}
	};

	public static final String NAME = "org.jboss.seam.deployment.EnumDeploymentHandler";

	private static final LogProvider log = Logging.getLogProvider(EnumDeploymentHandler.class);

	private Set<Class<? extends Enum<?>>> enums;

	public EnumDeploymentHandler() {
		enums = new HashSet<Class<? extends Enum<?>>>();
	}

	/**
	* Returns enum classes
	*/
	public Set<Class<? extends Enum<?>>> getEnums() {
		return Collections.unmodifiableSet(enums);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void postProcess(ClassLoader classLoader) {
		for (FileDescriptor fileDescriptor : getResources()) {
			String cname = filenameToClassName(fileDescriptor.getName());
			if (isOmitClass(cname)) {
				continue;
			}
			try {

				Class<?> clazz = Class.forName(cname);
				if (clazz.isEnum()) {
					if (log.isDebugEnabled()) {
						log.debug("Processing enum " + clazz.getName());
					}
					enums.add((Class<? extends Enum<?>>) clazz);
				}
			}
			// catching everything, to manage Errors
			catch (Throwable ignored) {
				// log.warn("Exception post-processing "+cname, t);
			}
		}
	}

	private boolean isOmitClass(String cname) {
		// To reduce verbose logging of NoClassDefFoundError on JBOSS AS 7.1.1 under some specific configurations.
		// thos classes / packages doesn't contains enums
		if (cname == null || "".equals(cname)) {
			return true;
		}
		return cname.startsWith("org.jboss.seam.mock.DB") || cname.startsWith("org.jboss.seam.mock.J")
				|| cname.startsWith("org.jboss.seam.jmx.AgentID") || cname.startsWith("org.jboss.seam.async.")
				|| cname.startsWith("org.jboss.seam.persistence.") || cname.startsWith("org.jboss.seam.drools.")
				|| cname.startsWith("org.jboss.seam.bpm.") || "org.jboss.seam.pageflow.Page".equals(cname);
	}

	private static String filenameToClassName(String filename) {
		return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
	}

	public String getName() {
		return NAME;
	}

	public DeploymentMetadata getMetadata() {
		return ENUM_METADATA;
	}
}
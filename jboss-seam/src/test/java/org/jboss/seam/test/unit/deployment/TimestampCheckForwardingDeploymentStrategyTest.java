package org.jboss.seam.test.unit.deployment;

import org.jboss.seam.deployment.DeploymentStrategy;
import org.jboss.seam.deployment.StandardDeploymentStrategy;
import org.jboss.seam.deployment.TimestampCheckForwardingDeploymentStrategy;
import org.jboss.seam.mock.MockServletContext;
import org.junit.Test;

public class TimestampCheckForwardingDeploymentStrategyTest {

	@Test
	public void test() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.getInitParameters().put("org.jboss.seam.deployment.OMIT_PACKAGES", "org.hibernate,com.lowagie");
		DeploymentStrategy deploymentStrategy = new StandardDeploymentStrategy(this.getClass().getClassLoader(), servletContext);

		DeploymentStrategy dt2 = new TimestampCheckForwardingDeploymentStrategy(deploymentStrategy);
		dt2.scan();

	}

}

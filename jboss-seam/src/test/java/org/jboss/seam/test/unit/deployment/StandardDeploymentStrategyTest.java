package org.jboss.seam.test.unit.deployment;

import org.jboss.seam.deployment.StandardDeploymentStrategy;
import org.jboss.seam.mock.MockServletContext;
import org.junit.Test;

public class StandardDeploymentStrategyTest {

	@Test
	public void test() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.getInitParameters().put("org.jboss.seam.deployment.OMIT_PACKAGES", "org.hibernate,com.lowagie");
		StandardDeploymentStrategy st = new StandardDeploymentStrategy(this.getClass().getClassLoader(), servletContext);
		st.scan();
	}
}

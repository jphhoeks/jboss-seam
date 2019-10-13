package org.jboss.seam.test.unit.deployment;

import org.jboss.seam.deployment.OmitPackageHelper;
import org.jboss.seam.mock.MockServletContext;
import org.junit.Assert;
import org.junit.Test;

public class OmitPackageHelperTest {

	public OmitPackageHelperTest() {
		super();
	}

	@Test
	public void test1() {
		MockServletContext servletContext = new MockServletContext();
		servletContext.getInitParameters().put("org.jboss.seam.deployment.OMIT_PACKAGES", "org.hibernate,com.lowagie");
		OmitPackageHelper helper = OmitPackageHelper.getInstance(servletContext);
		Assert.assertTrue(helper.acceptClass("org/apache/log4j/Log.class"));
		Assert.assertFalse(helper.acceptClass("org/hibernate/Hibernate.class"));
		Assert.assertFalse(helper.acceptClass("org/hibernate/subpackage/Hibernate.class"));
		Assert.assertTrue(helper.acceptPackage("org/apache/"));
		Assert.assertTrue(helper.acceptPackage("org/apache"));
		Assert.assertFalse(helper.acceptPackage("org/hibernate/"));
		Assert.assertFalse(helper.acceptPackage("org/hibernate"));
		Assert.assertFalse(helper.acceptPackage("org/hibernate/subpackage"));
		Assert.assertFalse(helper.acceptPackage("org/hibernate/subpackage/"));
	}
}

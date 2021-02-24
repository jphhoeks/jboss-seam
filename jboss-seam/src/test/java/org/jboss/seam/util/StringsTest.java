package org.jboss.seam.util;

import org.drools.core.util.StringUtils;
import org.junit.Test;
import org.junit.Assert;

public class StringsTest {
	
	public StringsTest() {
		super();
	}
	
	@Test
	public void split() {
		Assert.assertNotNull(Strings.split(null, null));
		Assert.assertEquals(0, Strings.split(null, null).length);
		Assert.assertNotNull(StringUtils.split("", null));
//		Assert.assertEquals(0, Strings.split("", null).length);
		Assert.assertEquals(3, Strings.split("a,b,c", ", \r\n\f\t").length);
		Assert.assertEquals(3, Strings.split("a,,b,c", ", \r\n\f\t").length);
		Assert.assertEquals(3, Strings.split("a, b,c", ", \r\n\f\t").length);
	}

}

package org.jboss.seam.jmx;

import org.junit.Test;

public class AgentIDTest {

	public AgentIDTest() {
		super();
	}
	
	
	@Test
	public void smoke() {
		AgentID.create();
	}
}

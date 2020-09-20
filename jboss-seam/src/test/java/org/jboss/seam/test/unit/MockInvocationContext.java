//$Id: MockInvocationContext.java 6435 2007-10-08 18:15:49Z pmuir $
package org.jboss.seam.test.unit;

import java.lang.reflect.Method;
import java.util.Map;

import org.jboss.seam.intercept.InvocationContext;

public class MockInvocationContext implements InvocationContext {
	
	public MockInvocationContext() {
		super();
	}
	
	@Override
	public Object getTarget() {
		//TODO
		return null;
	}
	@Override
	public Map getContextData() {
		//TODO
		return null;
	}
	@Override
	public Method getMethod() {
		//TODO
		return null;
	}
	@Override
	public Object[] getParameters() {
		//TODO
		return null;
	}
	@Override
	public Object proceed() throws Exception {
		return null;
	}
	@Override
	public void setParameters(Object[] params) {
		//TODO

	}

}

package org.jboss.seam.remoting.gwt;

import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.lang.reflect.Method;

import org.jboss.seam.util.CloneUtils;

/**
 * @author Tomaz Cerar
 * 
 * 
 * 
 */
public class SeamRPCRequest {
	private final java.lang.reflect.Method method;
	private final java.lang.Object[] parameters;
	private final Class[] parameterTypes;
	private final com.google.gwt.user.server.rpc.SerializationPolicy serializationPolicy;

	public SeamRPCRequest(Method method, Object[] parameters, Class[] parameterTypes, SerializationPolicy serializationPolicy) {
		this.method = method;
		this.parameters = CloneUtils.cloneArray(parameters);
		this.parameterTypes = CloneUtils.cloneArray(parameterTypes);
		this.serializationPolicy = serializationPolicy;
	}

	public Method getMethod() {
		return method;
	}

	public Object[] getParameters() {
		return CloneUtils.cloneArray(parameters);
	}

	public Class[] getParameterTypes() {
		return CloneUtils.cloneArray(parameterTypes);
	}

	public SerializationPolicy getSerializationPolicy() {
		return serializationPolicy;
	}
}

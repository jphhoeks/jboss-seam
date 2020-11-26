package org.jboss.seam;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.core.BijectionInterceptor;

/**
 * An exception that is thrown when {@link BijectionInterceptor} detects that a
 * component's dependencies cannot be injected due to a cyclic dependency. As
 * the exception is passed up the stack, the call sequence is recorded so that a
 * useful exception message can be constructed.
 * 
 * @author Matt Drees
 * 
 */
public class CyclicDependencyException extends IllegalStateException {

	private static final long serialVersionUID = 1L;
	/**
	 * stores the invocations in reverse call order
	 */
	private final List<String> invocations = new ArrayList<String>();
	private String tailComponentName;
	private boolean cycleComplete;
	
	public CyclicDependencyException() {
		super();
	}

	/**
	 * Records this invocation's component name and method to be displayed in
	 * {@link #getMessage()}, unless this invocation is not part of the detected
	 * cycle. This method will be successively called as the exception is
	 * propagated up the stack.
	 * 
	 * @param componentName
	 * @param method
	 */
	public void addInvocation(String componentName, Method method) {
		if (cycleComplete) {
			return;
		}

		if (invocations.isEmpty()) {
			tailComponentName = componentName;
		} else {
			if (tailComponentName.equals(componentName)) {
				cycleComplete = true;
			}
		}
		invocations.add(createInvocationLabel(componentName, method));
	}

	/**
	 * returns e.g. "foo.doSomething()"
	 */
	private String createInvocationLabel(String componentName, Method method) {
		StringBuilder invocationLabel = new StringBuilder(); 
		invocationLabel.append(componentName).append('.').append(method.getName()).append('(');
		int i = 1;
		for (Class<?> parameterType : method.getParameterTypes()) {
			invocationLabel.append(parameterType.getSimpleName());
			if (i < method.getParameterTypes().length) {
				invocationLabel.append(", ");
			}
			i++;
		}
		invocationLabel.append(')');
		return invocationLabel.toString();
	}

	@Override
	public String getMessage() {
		if (!cycleComplete) {
			return "Cyclic dependency found";
		} else {
			StringBuilder message = new StringBuilder();
			message.append("Injection into ") 
			.append(tailComponentName) 
			.append(" resulted in a dependency cycle, requiring the invocation of ")
			.append(invocations.get(0)) 
			.append(".  The complete cycle: ");
			for (int i = invocations.size() - 1; i >= 0; i--) {
				message.append(invocations.get(i));
				if (i != 0) {
					message.append(" -> ");
				}
			}
			return message.toString();
		}
	}

}

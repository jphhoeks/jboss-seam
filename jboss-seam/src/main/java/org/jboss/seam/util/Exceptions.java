package org.jboss.seam.util;

import javax.servlet.ServletException;

public class Exceptions {

	public static Exception getCause(Exception exception) {
		Throwable cause = getCauseThrowable(exception);
		if (cause instanceof Exception && !cause.equals(exception)) {
			return (Exception) cause;
		}
		return null;
	}

	public static Throwable getCauseThrowable(Throwable exception) {
		Throwable cause = null;
		try {
			if (EJB.EJB_EXCEPTION.isInstance(exception)) {
				cause = (Throwable) Reflections.getGetterMethod(EJB.EJB_EXCEPTION, "causedByException").invoke(exception);
			} else if (exception instanceof ServletException) {
				cause = ((ServletException) exception).getRootCause();
			} else {
				cause = exception.getCause();
			}
		} catch (Exception x) {
			return null;
		}
		return cause;
	}

}

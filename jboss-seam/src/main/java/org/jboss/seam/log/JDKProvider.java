package org.jboss.seam.log;

import java.util.logging.Level;
import java.util.logging.Logger;

class JDKProvider implements LogProvider {
	private final Logger logger;
	private final boolean isWrapped;

	JDKProvider(String category, boolean wrapped) {
		this.logger = Logger.getLogger(category);
		this.isWrapped = wrapped;
	}

	private void log(Level level, Object object, Throwable ex) {

		if (logger.isLoggable(level)) {
			Throwable dummyException = new Throwable();
			StackTraceElement locations[] = dummyException.getStackTrace();
			String className = "unknown";
			String methodName = "unknown";
			int depth = isWrapped ? 3 : 2;
			if (locations != null && locations.length > depth) {
				StackTraceElement caller = locations[depth];
				className = caller.getClassName();
				methodName = caller.getMethodName();
			}
			if (ex == null) {
				logger.logp(level, className, methodName, String.valueOf(object));
			} else {
				logger.logp(level, className, methodName, String.valueOf(object), ex);
			}
		}

	}

	@Override
	public void debug(Object object, Throwable t) {
		log(Level.FINE, object, t);
	}

	@Override
	public void debug(Object object) {
		log(Level.FINE, object, null);
	}

	@Override
	public void error(Object object, Throwable t) {
		log(Level.SEVERE, object, t);
	}

	@Override
	public void error(Object object) {
		log(Level.SEVERE, object, null);
	}

	@Override
	public void fatal(Object object, Throwable t) {
		log(Level.SEVERE, object, t);
	}

	@Override
	public void fatal(Object object) {
		log(Level.SEVERE, object, null);
	}

	@Override
	public void info(Object object, Throwable t) {
		log(Level.INFO, object, t);
	}

	@Override
	public void info(Object object) {
		log(Level.INFO, object, null);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(Level.FINE);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public boolean isFatalEnabled() {
		return logger.isLoggable(Level.SEVERE);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(Level.INFO);
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isLoggable(Level.FINER);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(Level.WARNING);
	}

	@Override
	public void trace(Object object, Throwable t) {
		log(Level.FINER, object, t);
	}

	@Override
	public void trace(Object object) {
		log(Level.FINER, object, null);
	}

	@Override
	public void warn(Object object, Throwable t) {
		log(Level.WARNING, object, t);
	}

	@Override
	public void warn(Object object) {
		log(Level.WARNING, object, null);
	}

}

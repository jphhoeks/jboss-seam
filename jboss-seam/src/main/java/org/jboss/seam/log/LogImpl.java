package org.jboss.seam.log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.jboss.seam.core.Interpolator;

/**
 * Implementation of the Log interface
 * 
 * @author Gavin King
 */
class LogImpl implements Log, Externalizable {
	private static final long serialVersionUID = -1664298172030714342L;

	private transient LogProvider log;
	private String category;

	public LogImpl() {
		super();
	}

	LogImpl(String category) {
		this.category = category;
		this.log = Logging.getLogProvider(category, true);
	}

	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	@Override
	public boolean isFatalEnabled() {
		return log.isFatalEnabled();
	}

	@Override
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	@Override
	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	@Override
	public void trace(Object object, Object... params) {
		if (isTraceEnabled()) {
			log.trace(interpolate(object, params));
		}
	}

	@Override
	public void trace(Object object, Throwable t, Object... params) {
		if (isTraceEnabled()) {
			log.trace(interpolate(object, params), t);
		}
	}

	@Override
	public void debug(Object object, Object... params) {
		if (isDebugEnabled()) {
			log.debug(interpolate(object, params));
		}
	}

	@Override
	public void debug(Object object, Throwable t, Object... params) {
		if (isDebugEnabled()) {
			log.debug(interpolate(object, params), t);
		}
	}

	@Override
	public void info(Object object, Object... params) {
		if (isInfoEnabled()) {
			log.info(interpolate(object, params));
		}
	}

	@Override
	public void info(Object object, Throwable t, Object... params) {
		if (isInfoEnabled()) {
			log.info(interpolate(object, params), t);
		}
	}

	@Override
	public void warn(Object object, Object... params) {
		if (isWarnEnabled()) {
			log.warn(interpolate(object, params));
		}
	}

	@Override
	public void warn(Object object, Throwable t, Object... params) {
		if (isWarnEnabled()) {
			log.warn(interpolate(object, params), t);
		}
	}

	@Override
	public void error(Object object, Object... params) {
		if (isErrorEnabled()) {
			log.error(interpolate(object, params));
		}
	}

	@Override
	public void error(Object object, Throwable t, Object... params) {
		if (isErrorEnabled()) {
			log.error(interpolate(object, params), t);
		}
	}

	@Override
	public void fatal(Object object, Object... params) {
		if (isFatalEnabled()) {
			log.fatal(interpolate(object, params));
		}
	}

	@Override
	public void fatal(Object object, Throwable t, Object... params) {
		if (isFatalEnabled()) {
			log.fatal(interpolate(object, params), t);
		}
	}

	private Object interpolate(Object object, Object... params) {
		if (object instanceof String) {
			try {
				object = Interpolator.instance().interpolate((String) object, params);
				return object;
			} catch (Exception e) {
				log.error("exception interpolating string: " + object, e);
			} 
			return object;
		} else {
			return object;
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		category = (String) in.readObject();
		log = Logging.getLogProvider(category, true);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(category);
	}
}

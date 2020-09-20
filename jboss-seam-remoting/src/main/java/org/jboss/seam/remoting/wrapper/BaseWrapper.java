package org.jboss.seam.remoting.wrapper;

import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Element;
import org.jboss.seam.remoting.CallContext;

/**
 * Base class for all Wrapper implementations.
 *
 * @author Shane Bryzak
 */
public abstract class BaseWrapper implements Wrapper {
	/**
	 * The path of this object within the result object graph
	 */
	protected String path;

	/**
	 * The call context
	 */
	protected CallContext context;

	/**
	 * The DOM4J element containing the value
	 */
	protected Element element;

	/**
	 * The wrapped value
	 */
	protected Object value;

	/**
	 * Sets the path.
	 *
	 * @param path String
	 */
	@Override
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets the wrapped value
	 *
	 * @param value Object
	 */
	@Override
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Returns the wrapped value
	 *
	 * @return Object
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the call context
	 */
	@Override
	public void setCallContext(CallContext context) {
		this.context = context;
	}

	/**
	 * Extracts a value from a DOM4J Element
	 *
	 * @param element Element
	 */
	@Override
	public void setElement(Element element) {
		this.element = element;
	}

	/**
	 * Default implementation does nothing
	 */
	@Override
	public void unmarshal() {
	}

	/**
	 * Default implementation does nothing
	 *
	 * @param out OutputStream
	 * @throws IOException
	 */
	@Override
	public void serialize(OutputStream out) throws IOException {
	}
}

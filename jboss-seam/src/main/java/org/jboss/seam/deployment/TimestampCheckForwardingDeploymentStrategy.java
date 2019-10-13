package org.jboss.seam.deployment;

/**
 * An accelerated version of the underlying strategy that uses the SimpleURLScanner
 * to determine the timestamp of the latest file.
 * 
 * @author Dan Allen
 */
public class TimestampCheckForwardingDeploymentStrategy extends ForwardingDeploymentStrategy {

	private DeploymentStrategy delegate;
	private long timestamp;

	public TimestampCheckForwardingDeploymentStrategy() {
		super();
	}

	public TimestampCheckForwardingDeploymentStrategy(DeploymentStrategy delegate) {
		this();
		this.delegate = delegate;
	}

	public boolean changedSince(long mark) {
		scan();
		return getTimestamp() > mark;
	}

	@Override
	public void scan() {
		if (getScanner() instanceof AbstractScanner) {
			AbstractScanner delegateScanner = (AbstractScanner) getScanner();
			delegateScanner.setTimestampScan(true);
			delegate().scan();
			this.timestamp = delegate().scanner.getTimestamp();
			delegateScanner.setTimestampScan(false);
		} else {
			delegate().scan();
		}
	}

	@Override
	protected void postScan() {
		// No-op
	}

	@Override
	protected DeploymentStrategy delegate() {
		return this.delegate;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

}
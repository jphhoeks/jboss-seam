package org.jboss.seam.drools;

/**
 * API for setting the result of a decision from rules in
 * a Drools decision handler.
 *
 */
public class Decision {
	private String outcome;
	
	public Decision() {
		super();
	}

	public String getOutcome() {
		return outcome;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}
}

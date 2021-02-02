package org.jboss.seam.async;

import java.util.Date;
import java.util.Objects;

/**
 * A "cron schedule" for a timed event executed by
 * the Quartz CronTrigger.
 * 
 * @author Michael Yuan
 *
 */
public class CronSchedule extends Schedule {
	private static final long serialVersionUID = 1L;
	private String cron;



	/**
	* @param duration the delay before the first event occurs
	* @param cron the unix cron string to control how the events are repeated
	*/
	public CronSchedule(Long duration, String cron) {
		super(duration);
		this.cron = cron;
	}

	/**
	* @param expiration the datetime at which the first event occurs
	* @param cron the unix cron string to control how the events are repeated
	*/
	public CronSchedule(Date expiration, String cron) {
		super(expiration);
		this.cron = cron;
	}

	CronSchedule(Long duration, Date expiration, String cron, Date finalExpiration) {
		super(duration, expiration, finalExpiration);
		this.cron = cron;
	}
	
	String getCron() {
		return cron;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		if (object instanceof CronSchedule) {
			CronSchedule other = (CronSchedule) object;
			boolean superEquals = super.equals(other);
			if (!superEquals) {
				return false;
			}
			if (this.cron == null) {
				return other.cron == null;
			}
			return this.cron.equals(other.cron);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(cron);
		return result;
	}
	
	
	

}

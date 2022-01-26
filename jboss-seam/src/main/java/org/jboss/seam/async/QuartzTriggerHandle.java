package org.jboss.seam.async;

import java.io.Serializable;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

/**
 * Provides control over the Quartz Job.
 * 
 * @author Michael Yuan
 *
 */
public class QuartzTriggerHandle implements Serializable {
	private static final long serialVersionUID = 1L;

	private final TriggerKey key;

	// Hold a transient reference to the scheduler to allow control of the
	// scheduler outside of Seam contexts (useful in a testing context)
	private transient Scheduler scheduler;

	public QuartzTriggerHandle(TriggerKey key) {
		super();
		this.key = key;
	}
	
	public QuartzTriggerHandle(String triggerName) {		 
		super();
		this.key = new TriggerKey(triggerName);
	}

	public void cancel() throws SchedulerException {
		getScheduler().unscheduleJob(key);
	}

	public void pause() throws SchedulerException {
		getScheduler().pauseTrigger(key);
	}

	public Trigger getTrigger() throws SchedulerException {
		return getScheduler().getTrigger(key);
	}

	public void resume() throws SchedulerException {
		getScheduler().resumeTrigger(key);
	}

	private Scheduler getScheduler() {
		if (scheduler == null) {
			scheduler = QuartzDispatcher.instance().getScheduler();
		}
		return scheduler;
	}

}

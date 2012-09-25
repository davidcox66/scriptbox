package org.scriptbox.box.plugins.quartz;

import org.quartz.JobDetail;
import org.scriptbox.box.container.BoxContext;

public interface QuartzListener {

	public void jobStarted( BoxContext context, JobDetail detail );
	public void jobCompleted( BoxContext context, JobDetail detail );
}

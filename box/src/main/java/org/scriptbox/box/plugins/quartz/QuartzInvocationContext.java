package org.scriptbox.box.plugins.quartz;

import java.util.Iterator;
import java.util.List;

import org.quartz.JobDetail;
import org.scriptbox.box.container.BoxContext;

public class QuartzInvocationContext {

	private BoxContext boxContext;
	private QuartzExecBlock block;
	private JobDetail detail;
	private Iterator<QuartzListener> iter;
	private boolean evaluated;
	
	public QuartzInvocationContext( BoxContext boxContext, QuartzExecBlock block, JobDetail detail, List<QuartzListener> listeners ) {
		this.boxContext = boxContext;
		this.block = block;
		this.detail = detail;
		this.iter = listeners.iterator();
	}

	public BoxContext getBoxContext() {
		return boxContext;
	}

	public JobDetail getDetail() {
		return detail;
	}

	public void next() throws Exception {
		if( iter.hasNext() ) {
			QuartzListener listener = iter.next();
			listener.jobStarted( this );
		}
		else {
			if( evaluated ) {
				throw new RuntimeException( "Cannot run job multiple times");
			}
			evaluated = true;
			block.with( block );
		}
	}
}

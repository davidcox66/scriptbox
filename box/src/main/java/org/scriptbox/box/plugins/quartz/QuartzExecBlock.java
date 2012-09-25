package org.scriptbox.box.plugins.quartz;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.BasicExecBlock;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.util.common.collection.Iterators;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzExecBlock extends BasicExecBlock<ExecRunnable> implements Job {

	private static Logger LOGGER = LoggerFactory.getLogger( QuartzExecBlock.class );
	
	private QuartzPlugin plugin;
	private BoxContext context;
	private JobDetail detail;

	// Only called by quartz
	public QuartzExecBlock() {
	}
	
	public QuartzExecBlock( QuartzPlugin plugin, BoxContext context ) {
		this.plugin = plugin;
		this.context = context;
	}
	
	public QuartzPlugin getPlugin() {
		return plugin;
	}

	public JobDetail getDetail() {
		return detail;
	}
	public void setDetail(JobDetail detail) {
		this.detail = detail;
	}

	public void run() throws Exception {
		BoxContext.with( context, new ParameterizedRunnable<BoxContext>() {
			public void run( BoxContext ctx ) throws Exception {
				try {
					Iterators.callAndCollectExceptions(plugin.getListeners(), new ParameterizedRunnable<QuartzListener>() {
						public void run( QuartzListener listener ) throws Exception {
							listener.jobStarted(context, detail);
							
						}
					} ).raiseException("Failed notifying QuartzListeners of job start");
					QuartzExecBlock.super.run();
				}
				finally {
					Iterators.callAndCollectExceptions(plugin.getListeners(), new ParameterizedRunnable<QuartzListener>() {
						public void run( QuartzListener listener ) throws Exception {
							listener.jobCompleted(context, detail);
							
						}
					} ).raiseException("Failed notifying QuartzListeners of job complete");
					
				}
			}
		} );
	}
	public void execute( JobExecutionContext ctx ) {
		try {
			QuartzExecBlock block = (QuartzExecBlock)ctx.getJobDetail().getJobDataMap().get("block");
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "execute: block=" + block ); }
			block.run();
		}
		catch( Exception ex ) {
			LOGGER.error( "Error executing job", ex );
		}
	}
}

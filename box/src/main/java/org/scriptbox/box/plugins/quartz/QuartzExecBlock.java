package org.scriptbox.box.plugins.quartz;

import org.quartz.Job;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.BasicExecBlock;
import org.scriptbox.box.exec.ExecRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzExecBlock extends BasicExecBlock<ExecRunnable> implements Job {

	private static Logger LOGGER = LoggerFactory.getLogger( QuartzExecBlock.class );
	
	private BoxContext context;
	private JobDetail detail;

	// Only called by quartz
	public QuartzExecBlock() {
	}
	
	public QuartzExecBlock( BoxContext context ) {
		this.context = context;
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
				QuartzExecBlock.super.run();
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

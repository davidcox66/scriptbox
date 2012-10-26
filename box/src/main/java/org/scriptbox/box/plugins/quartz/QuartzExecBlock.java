package org.scriptbox.box.plugins.quartz;

import java.util.concurrent.atomic.AtomicBoolean;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.BasicExecBlock;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzExecBlock extends BasicExecBlock<ExecRunnable> implements Job {

	private static Logger LOGGER = LoggerFactory.getLogger( QuartzExecBlock.class );
	
	private QuartzPlugin plugin;
	private BoxContext context;
	private JobDetail detail;

	private AtomicBoolean busy = new AtomicBoolean();
	
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
				QuartzInvocationContext qctx = new QuartzInvocationContext(ctx, QuartzExecBlock.this, detail, plugin.getListeners() );
				qctx.next();
			}
		} );
	}
	public void execute( JobExecutionContext ctx ) {
		
		JobDataMap map = ctx.getJobDetail().getJobDataMap();
		int id = map.getInt("id");
		QuartzExecBlock block = (QuartzExecBlock)map.get("block");
		
		if( busy.compareAndSet(false, true) ) {
			try {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "execute: id=" + id + ", block=" + block ); }
				block.run();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error executing job", ex );
			}
			finally {
				busy.set(false);
			}
		}
		else {
			LOGGER.info( "execute: already in the middle of processing job, will skip this iteration: id=" + id + ", block=" + block );
		}
	}
}

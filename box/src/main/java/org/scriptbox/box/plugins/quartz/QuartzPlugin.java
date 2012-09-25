package org.scriptbox.box.plugins.quartz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.controls.BoxService;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class QuartzPlugin extends BoxContextInjectingListener {

	private static final AtomicInteger count = new AtomicInteger();
	
	private List<QuartzListener> listeners;
	
	public QuartzPlugin() throws SchedulerException {
	}

	
	public List<QuartzListener> getListeners() {
		return listeners;
	}


	public void setListeners(List<QuartzListener> listeners) {
		this.listeners = listeners;
	}


	public void contextCreated(BoxContext context) throws Exception {
		
		Properties props = new Properties();
		props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID );
		props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, context.getName() );
		props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool" );
		props.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, "org.quartz.simpl.RAMJobStore" );
		props.put("org.quartz.threadPool.threadCount", "3" );
		
		final Scheduler scheduler = new StdSchedulerFactory(props).getScheduler();
		
		final Lookup beans = context.getBeans();
		beans.put( "quartz.blocks", new HashMap<Integer,QuartzExecBlock>() );
		beans.put( "quartz.scheduler", scheduler );
		beans.put( "quartz.schedulerService", new BoxService() {
			public void start( List arguments ) throws Exception {
				scheduler.start();
			}
			public void shutdown() throws Exception {
				scheduler.shutdown();
			}
			public void stop() throws Exception {
				scheduler.standby();
			}
			public String status() throws Exception {
				return "QuartzScheduler{ " + scheduler + "}";
			}
		});
		setInjectors( context.getBox().getInjectorsOfType(QuartzInjector.class) );
		super.contextCreated( context );
	}
	
	@Override
	public void contextShutdown(BoxContext context) throws Exception {
		Scheduler scheduler = context.getBeans().get( "quartz.scheduler", Scheduler.class );
		if( scheduler != null ) {
			scheduler.shutdown();
		}	
		super.contextShutdown( context );
	}

	public int getNextId() {
		return count.addAndGet(1);
	}
	@SuppressWarnings("unchecked")
	public void configureJob( BoxContext context, int cnt, Trigger trigger, QuartzExecBlock block ) throws SchedulerException {
		JobDetail detail = new JobDetail("detail " + cnt, QuartzExecBlock.class);
		JobDataMap map = detail.getJobDataMap();
		map.put("id", cnt );
		map.put("block", block );
		
		block.setDetail( detail );
		Lookup beans = context.getBeans();
		beans.getEx( "quartz.blocks", Map.class ).put( cnt, block );
		beans.getEx( "quartz.scheduler",Scheduler.class).scheduleJob( detail, trigger );
	}

}

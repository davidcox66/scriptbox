package org.scriptbox.selenium.ext.misc;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 5/28/15.
 */
public class QuartzExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzExtension.class);
    private static final AtomicInteger count = new AtomicInteger();

    private Scheduler scheduler;

    public void configure() {
        bind(context.getBinding());
    }

    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "at");
        BindUtils.bind(binding, this, "every");
        BindUtils.bind(binding, this, "once");
        BindUtils.bind(binding, this, "shutdown");
    }

    public void at( String when, final Closure closure ) throws Exception {
        int cnt = count.getAndIncrement();
        CronTrigger trigger = new CronTrigger("at " + cnt );
        trigger.setCronExpression( new CronExpression(when) );
        schedule(cnt, trigger, closure);
    }

    public void every( int when, Closure closure ) throws Exception {
        int cnt = count.getAndIncrement();
        Trigger trigger = new SimpleTrigger("every " + cnt, SimpleTrigger.REPEAT_INDEFINITELY, when );
        schedule(cnt, trigger, closure);
    }

    public void once( Closure closure ) throws Exception {
        int cnt = count.getAndIncrement();
        Trigger trigger = new SimpleTrigger("once " + cnt );
        schedule(cnt, trigger, closure);
    }

    synchronized public void shutdown() {
        try {
            if( scheduler != null ) {
                scheduler.shutdown();
                scheduler = null;
            }
        }
        catch( Exception ex ) {
            throw new RuntimeException( "Error shutting down scheduler", ex );
        }
    }

    synchronized private Scheduler getScheduler() {
        if (scheduler == null) {
            try {
                Properties props = new Properties();
                props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_ID, StdSchedulerFactory.AUTO_GENERATE_INSTANCE_ID);
                props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "selenium");
                props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
                props.put(StdSchedulerFactory.PROP_JOB_STORE_CLASS, "org.quartz.simpl.RAMJobStore");
                props.put("org.quartz.threadPool.threadCount", "" + 1);

                scheduler = new StdSchedulerFactory(props).getScheduler();
                scheduler.start();
            }
            catch (Exception ex) {
                throw new RuntimeException("Error starting scheduler", ex);
            }
        }
        return scheduler;
    }

    public void schedule( int cnt, Trigger trigger, Closure block ) throws SchedulerException {
        JobDetail detail = new JobDetail("detail " + cnt, SeleniumJob.class);
        JobDataMap map = detail.getJobDataMap();
        map.put("id", cnt );
        map.put("block", block );

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "schedule: id=" + cnt + ", trigger=" + trigger + ", block=" + block );
        }
        getScheduler().scheduleJob(detail, trigger);
    }

}

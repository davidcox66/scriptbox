package org.scriptbox.selenium.ext;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 5/28/15.
 */
public class QuartzExtension implements Bindable, SeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzExtension.class);
    private static final AtomicInteger count = new AtomicInteger();

    private Scheduler scheduler;

    public void init( SeleniumExtensionContext ctx ) {
        try {
            if (ctx.getCommandLine().consumeArg("quartz")) {
                bind(ctx.getBinding());
                start();
            }
        }
        catch( Exception ex ) {
            throw new RuntimeException( "Failed initializing Quartz plugin", ex );
        }
    }

    @Override
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

    public void shutdown() {
        try {
            scheduler.shutdown();
        }
        catch( Exception ex ) {
            throw new RuntimeException( "Error shutting down scheduler", ex );
        }
    }

    private void start() {
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
        catch( Exception ex ) {
            throw new RuntimeException( "Error starting scheduler", ex );
        }
    }

    public void schedule( int cnt, Trigger trigger, Closure block ) throws SchedulerException {
        JobDetail detail = new JobDetail("detail " + cnt, SeleniumJob.class);
        JobDataMap map = detail.getJobDataMap();
        map.put("id", cnt );
        map.put("block", block );

        if( LOGGER.isDebugEnabled() ) {
            LOGGER.debug( "schedule: id=" + cnt + ", trigger=" + trigger + ", block=" + block );
        }
        scheduler.scheduleJob(detail, trigger);
    }

}

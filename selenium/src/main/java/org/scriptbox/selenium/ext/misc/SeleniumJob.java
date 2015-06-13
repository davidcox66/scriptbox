package org.scriptbox.selenium.ext.misc;

import groovy.lang.Closure;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by david on 5/31/15.
 */
public class SeleniumJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Map<String,Object> map = context.getJobDetail().getJobDataMap();
        Integer id = (Integer)map.get( "id" );
        Closure closure = (Closure)map.get( "block" );
        LOGGER.debug( "exeucte: running job: " + id );
        try {
            closure.call();
        }
        catch( Exception ex ) {
            LOGGER.error( "Error running job", ex );
        }
    }
}

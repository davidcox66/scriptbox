package org.scriptbox.panopticon.inbox;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboxGroovy extends Inbox {

	private static final Logger LOGGER = LoggerFactory.getLogger( InboxGroovy.class );
	
	public InboxGroovy( BoxContext context ) {
		super( context );
	}

	public void deliver( final Closure closure ) {
		super.deliver( new ParameterizedRunnable<Object[]>() {
			public void run( Object[] args ) {
				call( closure, args );
			}
		});
	}
	
	public void blackout( String start, String end, final Closure closure ) throws Exception {
		blackout( start, end, false, closure );
	}
	
	public void blackout( String start, String end, boolean all, final Closure closure ) throws Exception {
		super.blackout( start, end, all, new ParameterizedRunnableWithResult<Boolean,Object[]>() {
			public Boolean run( Object[] args ) throws Exception {
				return Closures.coerceToBoolean( call(closure,args) );
			}
		} );
	}
	
	public void throttle( final int minutes, final Closure closure ) throws Exception {
		throttle( minutes, false, closure );
	}
	
	public void throttle( final int minutes, final boolean all, final Closure closure ) throws Exception {
		super.throttle( minutes, all, new ParameterizedRunnableWithResult<Boolean,Object[]>() {
			public Boolean run( Object[] args ) throws Exception {
				return Closures.coerceToBoolean( call(closure,args) );
			}
		} );
	}
	
	public void predicate( final Closure closure ) {
		predicate( false, closure );
	}
	
	public void predicate( final boolean all, final Closure closure ) {
		super.predicate( all, new ParameterizedRunnableWithResult<Boolean,Object[]>() {
			public Boolean run( Object[] args ) throws Exception {
				return Closures.coerceToBoolean( call(closure,args) );
			}
		} );
	}
	
	private Object call( Closure closure, Object[] args ) {
		if( closure.getMaximumNumberOfParameters() == 0 ) {
			return closure.call();
		}
		else if( closure.getMaximumNumberOfParameters() == 1 ) {
			return closure.call( args[0] );
		}
		else {
			return closure.call( args[0], args[1] );
		}
			
	}
}

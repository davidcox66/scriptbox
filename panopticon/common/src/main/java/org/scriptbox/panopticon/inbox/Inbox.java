package org.scriptbox.panopticon.inbox;


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
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inbox {

	private static final Logger LOGGER = LoggerFactory.getLogger( Inbox.class );
	
	private static AtomicInteger counter = new AtomicInteger();
	private static DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
	
	private int maxDeliveryRetentionMinutes = 120;
	private int maxMessages = 10000;
	private LinkedList<Delivery> deliveries = new LinkedList<Delivery>();
	
	private boolean scheduled;
	private BoxContext context;
	private List<ParameterizedRunnable<Object[]>> receivers = new ArrayList<ParameterizedRunnable<Object[]>>();
	private List<ParameterizedRunnableWithResult<Boolean,List<Message>>> predicates = 
		new ArrayList<ParameterizedRunnableWithResult<Boolean,List<Message>>>();

	public static class InboxJob implements Job {
		public void execute( JobExecutionContext ctx ) throws JobExecutionException {
			JobDataMap map = ctx.getJobDetail().getJobDataMap();
			final Inbox inbox = (Inbox)map.get("inbox");
			try {
				BoxContext.with(inbox.context, new ParameterizedRunnable<BoxContext>() {
					public void run( BoxContext ctx ) {
						synchronized( inbox ) {
							inbox.processDelivery( inbox.getUndeliveredMessages() );
						}
					}
				} );
			}
			catch( Exception ex ) {
				throw new JobExecutionException( "Failed processing delivery", ex );
			}
		}
		
	}
	
	public Inbox( BoxContext context ) {
		this.context = context;
		deliveries.add( new Delivery() );
	}
	public List<Message> getUndeliveredMessages() {
		return getCurrentDelivery().getMessages();
	}
	public int getCountUndeliveredMessages() {
		return getUndeliveredMessages().size();
	}
	
	public boolean isAnyUndeliveredMessages() {
		return getCountUndeliveredMessages() > 0;
	}
	public Delivery getCurrentDelivery() {
		return deliveries.getFirst();
	}
	public Delivery getPreviousDelivery() {
		return deliveries.size() > 1 ? deliveries.get(1) : null;
	}
	public List<Delivery> getDeliveries() {
		return deliveries;
	}
	
	public int getCountDeliveriesInPastMinutes( int minutes ) {
		DateTime past = getDateInPast(minutes);
		int count = 0;
		for( Delivery del : deliveries ) {
			if( del.getTime() != null && del.getTime().isBefore(past) ) {
				break;
			}
			count++;
		}
		return count;
	}
	
	synchronized public void send( String source, int priority, Object data ) {
		startSchedulerIfNeeded();
		
		Message msg = new Message(source,priority,data);
		Delivery current = getCurrentDelivery();
		current.setMinPriority( Math.min(current.getMinPriority(),priority) );
		current.setMaxPriority( Math.max(current.getMaxPriority(),priority) );
		current.getMessages().add( msg );
		enforceMessageLimit();
		
		List<Message> messagesToEvaluate = new ArrayList<Message>(1);
		messagesToEvaluate.add( msg );
		processDelivery( messagesToEvaluate );
	}

	synchronized public void deliver( ParameterizedRunnable<Object[]> closure ) {
		receivers.add( closure );
	}
	
	public void blackout( String start, String end, final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) throws Exception {
		blackout( start, end, false, closure );
	}
	
	synchronized public void blackout( String start, String end, final boolean all, final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) throws Exception {
		final LocalTime startTime = fmt.parseDateTime(start).toLocalTime();
		final LocalTime endTime = fmt.parseDateTime(end).toLocalTime();
		
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Message>>() {
			public Boolean run( List<Message> messagesToEvaluate ) throws Exception {
				if( isBetween(startTime,endTime) ) {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "blackout: in blackout period: " + startTime + "-" + endTime ); }
					boolean ret = isPassed(closure,messagesToEvaluate,all); 
					if( LOGGER.isDebugEnabled() ) {
						LOGGER.debug( "blackout: " + (ret ? "allowing " : "suspending ") + messagesToEvaluate.size() + 
							" message during blackout period" );
					}
					return ret;
				}
				else {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "blackout: outside blackout period: " + startTime + "-" + endTime ); }
				}
				return true;
			}
		} );
	}
	
	public void throttle( final int minutes, final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) throws Exception {
		throttle( minutes, false, closure );
	}
	
	synchronized public void throttle( final int minutes, final boolean all, final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) throws Exception {
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Message>>() {
			public Boolean run( List<Message> messagesToEvaluate ) throws Exception {
				Delivery previous = getPreviousDelivery();
				if( previous != null ) {
					DateTime past = getDateInPast(minutes);
					if( previous.getTime().isAfter(past) ) {
						if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "throttle: within throttle period previous: " + previous.getTime() + " is after: " + past ); }
						boolean ret = isPassed(closure, messagesToEvaluate,all);
						if( LOGGER.isDebugEnabled() ) {
							LOGGER.debug( "throttle: " + (ret ? "allowing " : "suspending ") + messagesToEvaluate.size() + " messages" ); 
						}
						return ret;
					}
					else {
						if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "throttle: outside throttle period previous: " + previous.getTime() + " is after: " + past ); }
					}
				}
				else {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "throttle: no previous deliveries" ); }
				}
				return true;
			}
		} );
	}
	
	public void predicate( final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) {
		predicate( false, closure );
	}
	
	synchronized public void predicate( final boolean all, final ParameterizedRunnableWithResult<Boolean,Object[]> closure ) {
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Message>>() {
			public Boolean run( List<Message> messagesToEvaluate ) throws Exception {
				return isPassed( closure, messagesToEvaluate, all );
			}
		} );
	}

	
	private void processDelivery( List<Message> messagesToEvaluate ) {
		try {
			int count = getCountUndeliveredMessages();
			if( count > 0 && isAllPredicatesPassed(messagesToEvaluate) ) {
				try {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "processDelivery: delivering " + count + " messages" ); }
					Delivery current = getCurrentDelivery();
					current.setTime( new DateTime() );
					current.setCount( count );
					Object[] args = new Object[2];
					args[0] = current.getMessages();
					args[1] = this;
					for( ParameterizedRunnable<Object[]> receiver : receivers ) {
						try {
							receiver.run( args );
						}
						catch( Exception ex ) {
							LOGGER.error( "Error raised from message receiver", ex );
						}
					}
				}
				finally {
					archiveDelivery();
				}
			}
		}
		catch( Exception ex ) {
			LOGGER.error( "Error delivering messages", ex );
		}
	}

	private void archiveDelivery() {
		Delivery previous = getPreviousDelivery();
		if( previous != null ) {
			previous.setMessages( null ); // Only retain the actual messages for the current and previous
		}
		deliveries.addFirst(new Delivery());

		// Drop deliveries beyond the rentention window
		DateTime oldest = new DateTime();
		oldest.plusMinutes( -maxDeliveryRetentionMinutes );
		Iterator<Delivery> iter = deliveries.descendingIterator();
		while( iter.hasNext() ) {
			Delivery del = iter.next();
			if( del.getTime() == null || del.getTime().isBefore(oldest) ) {
				break;
			}
			LOGGER.debug( "archiveDelivery: discarding delivery: " + del.getTime() );
			iter.remove();
		}
	}
	
	private void enforceMessageLimit() {
		Delivery current = getCurrentDelivery();
		while( current.getMessages().size() > maxMessages ) {
			Message message = current.getMessages().remove(0);
			LOGGER.warn( "enforceMessageLimit: exceeded message queue limit - removing: " + message );
		}
	}
	
	private boolean isAllPredicatesPassed( List<Message> messagesToEvaluate ) throws Exception {
		for( ParameterizedRunnableWithResult<Boolean,List<Message>> predicate : predicates ) {
			if( !predicate.run(messagesToEvaluate) ) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isPassed(ParameterizedRunnableWithResult<Boolean,Object[]> closure, List<Message> messagesToEvaluate, boolean all) throws Exception {
		return all ? isAllMessagesPassed(closure) : isAnyMessagePassed(closure, messagesToEvaluate);
	}
	
	private boolean isAnyMessagePassed(ParameterizedRunnableWithResult<Boolean,Object[]> closure, List<Message> messagesToEvaluate) throws Exception {
		for( Message message : messagesToEvaluate ) {
			if( isMessagePassed(closure, message) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAllMessagesPassed(ParameterizedRunnableWithResult<Boolean,Object[]> closure) throws Exception {
		for( Message message : getUndeliveredMessages() ) {
			if( !isMessagePassed(closure, message) ) {
				return false;
			}
		}
		return true;
	}

	private boolean isMessagePassed(ParameterizedRunnableWithResult<Boolean,Object[]> closure, Message message) throws Exception {
		Object[] args = new Object[2];
		args[0] = message;
		args[1] = this;
		return closure.run( args );
	}
	
	private boolean isBetween( LocalTime start, LocalTime end ) {
		LocalTime now = new LocalTime();

		// straddles evening day change
		if( start.isAfter(end) ) {
			LocalTime last = new LocalTime( 23, 59, 59, 999 );
			return 
				(!start.isAfter(now) && !last.isBefore(now)) ||
				(!LocalTime.MIDNIGHT.isAfter(now) && !end.isBefore(now));
		}
		else {
			return !start.isAfter(now) && !end.isBefore(now); 
			
		}
	}

	private DateTime getDateInPast( int minutes ) {
		DateTime past = new DateTime();
		return past.plusMinutes( -minutes );
	}
	
	private void startSchedulerIfNeeded() {
		if( !scheduled ) {
			try {
				Trigger trigger = new SimpleTrigger("every " + counter.addAndGet(1), SimpleTrigger.REPEAT_INDEFINITELY, 60*1000 );
				Scheduler scheduler = getScheduler();
				int cnt = counter.addAndGet(1);
				
				JobDetail detail = new JobDetail("inbox" + cnt, InboxJob.class);
				JobDataMap map = detail.getJobDataMap();
				map.put("id", cnt );
				map.put("inbox", this );
			    
				scheduler.scheduleJob( detail, trigger );
				scheduled = true;
			}
			catch( Exception ex ) {
				throw new RuntimeException( "Unable to start scheduler", ex );
			}
		}
	}
	
	private Scheduler getScheduler() {
		return BoxContext.getCurrentContext().getBeans().get( "quartz.scheduler", Scheduler.class );
	}
}

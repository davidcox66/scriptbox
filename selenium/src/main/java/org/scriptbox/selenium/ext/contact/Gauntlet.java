package org.scriptbox.selenium.ext.contact;


import groovy.lang.Closure;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;


public class Gauntlet {

	private static final Logger LOGGER = LoggerFactory.getLogger( Gauntlet.class );
	
	private static AtomicInteger counter = new AtomicInteger();
	private static DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");
	
	private boolean immediate;
	private int maxDeliveryRetentionMinutes = 120;
	private int maxNotifications = 1000;
	private LinkedList<Delivery> deliveries = new LinkedList<Delivery>();
	
	private List<Closure> receivers = new ArrayList<Closure>();
	private List<Closure> discarders = new ArrayList<Closure>();
	private List<Closure> filters =  new ArrayList<Closure>();
	private List<ParameterizedRunnableWithResult<Boolean,List<Notification>>> predicates = new ArrayList<ParameterizedRunnableWithResult<Boolean,List<Notification>>>();

    private Thread scheduler;

    public Gauntlet() {
        this( false );
    }

	public Gauntlet(boolean immediate) {
		this.immediate = immediate;
		deliveries.add( new Delivery() );
	}
	
	public boolean isImmediate() {
		return immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}

	public LinkedList<Notification> getUndeliveredNotifications() {
		return getCurrentDelivery().getNotifications();
	}
	
	public int getCountUndeliveredNotifications() {
		return getUndeliveredNotifications().size();
	}
	
	public boolean isAnyUndeliveredNotifications() {
		return getCountUndeliveredNotifications() > 0;
	}
	
	public Delivery getCurrentDelivery() {
		return deliveries.getFirst();
	}
	
	public long getMinutesSinceLastDelivery() {
		Delivery previous = getPreviousDelivery();
		if( previous != null ) {
			DateTime now = new DateTime();
			DateTime last = previous.getTime();
			Duration dur = new Duration( last, now );
			return dur.getStandardMinutes();
		}
		return 0;
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
			if( del.getTime() != null ) {
				if( del.getTime().isBefore(past) ) {
					break;
				}
				count++;
			}
		}
		return count;
	}
	
	synchronized public void send( String source, int priority, Object data ) throws Exception {
        start();

		Notification msg = new Notification(source,priority,data);
		if( isAllDiscardersPassed(msg) ) {
			Delivery current = getCurrentDelivery();
			current.setMinPriority( Math.min(current.getMinPriority(),priority) );
			current.setMaxPriority( Math.max(current.getMaxPriority(),priority) );
			current.getNotifications().addLast( msg );
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "send: added notification: " + msg ); }
			
			enforceNotificationLimit();
	
			if( immediate ) {
				List<Notification> notificationsToEvaluate = new ArrayList<Notification>(1);
				notificationsToEvaluate.add( msg );
				processDelivery( notificationsToEvaluate );
			}
		}
		else {
			if( LOGGER.isDebugEnabled() ) {
				LOGGER.debug( "send: discarding notification: " + msg );
			}
		}
	}

	synchronized public void deliver( Closure closure ) {
		receivers.add( closure );
	}
	
	public void blackout( String start, String end, final Closure closure ) throws Exception {
		between( start, end, false, closure );
	}
	
	public void blackout( String start, String end, final boolean all, final Closure closure ) throws Exception {
		between( start, end, all, closure );
	}
	
	public void between( String start, String end, final Closure closure ) throws Exception {
		between( start, end, false, closure );
	}
	
	synchronized public void between( String start, String end, final boolean all, final Closure closure ) throws Exception {
		final LocalTime startTime = fmt.parseDateTime(start).toLocalTime();
		final LocalTime endTime = fmt.parseDateTime(end).toLocalTime();
		
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Notification>>() {
			public Boolean run( List<Notification> notificationsToEvaluate ) throws Exception {
				if( isBetween(startTime,endTime) ) {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "blackout: in blackout period: " + startTime + "-" + endTime ); }
					boolean ret = isPassed(closure,notificationsToEvaluate,all); 
					if( LOGGER.isDebugEnabled() ) {
						LOGGER.debug( "blackout: " + (ret ? "allowing " : "suspending ") + getCountUndeliveredNotifications() + 
							" notification during blackout period" );
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
	
	synchronized public void discard( final Closure closure ) throws Exception {
	    discarders.add( closure );
	}
	
	synchronized public void filter( final Closure closure ) throws Exception {
	    filters.add( closure );
	}
	
	public void backoff( final int minutes, final Closure closure ) throws Exception {
		backoff( minutes, minutes, closure );
	}
	
	synchronized public void backoff( final int initial, final int increment, final Closure closure ) throws Exception {
		class Backoff extends Closure {
            public Backoff() {
                super( Gauntlet.this );
            }

			public int minutes=initial;
			boolean rejected;

			public void run( Object[] args ) {
				if( rejected ) {
					minutes += increment;
					rejected = false;
				}
			}
			public boolean check() {
				long sinceLast = getMinutesSinceLastDelivery(); 
				if( sinceLast > 0 ) {
					// We are trying to send again too soon 
					if( sinceLast <= minutes ) {
						rejected = true;
						return false;
					}
					// We have been quiet for the current duration so we will reset back to original minutes
					else {
						rejected = false;
						minutes = initial;
						return true;
					}
				}
				else {
					// nothing delivered
					return true;
				}
			}
		}
		final Backoff bo = new Backoff();
		receivers.add( bo );
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Notification>>() {
			public Boolean run( List<Notification> notificationsToEvaluate ) throws Exception {
				return bo.check();
			}
		} );
	}
	
	public void throttle( final int minutes, final Closure closure ) throws Exception {
		throttle( minutes, false, closure );
	}
	
	synchronized public void throttle( final int minutes, final boolean all, final Closure closure ) throws Exception {
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Notification>>() {
			public Boolean run( List<Notification> notificationsToEvaluate ) throws Exception {
				Delivery previous = getPreviousDelivery();
				if( previous != null ) {
					DateTime past = getDateInPast(minutes);
					if( previous.getTime().isAfter(past) ) {
						if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "throttle: within throttle period previous: " + previous.getTime() + " is after: " + past ); }
						boolean ret = isPassed(closure, notificationsToEvaluate,all);
						if( LOGGER.isDebugEnabled() ) {
							LOGGER.debug( "throttle: " + (ret ? "allowing " : "suspending ") + notificationsToEvaluate.size() + " notifications" ); 
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
	
	public void predicate( final Closure closure ) {
		predicate( false, closure );
	}
	
	synchronized public void predicate( final boolean all, final Closure closure ) {
		predicates.add( new ParameterizedRunnableWithResult<Boolean,List<Notification>>() {
			public Boolean run( List<Notification> notificationsToEvaluate ) throws Exception {
				return isPassed( closure, notificationsToEvaluate, all );
			}
		} );
	}

	private boolean isAllDiscardersPassed( Notification notification ) throws Exception {
		for( Closure c : discarders ) {
			if( !coerceToBoolean(call(c,notification)) ) {
				return false;
			}
		}
		return true;
	}
	
	synchronized private void processDelivery( List<Notification> notificationsToEvaluate ) {
		int count = getCountUndeliveredNotifications();
		boolean anyNotifications = count > 0 || notificationsToEvaluate.size() > 0;
		try {
			if( LOGGER.isDebugEnabled() ) { 
				if( anyNotifications ) {
					LOGGER.debug( "processDelivery: notification count: " + count + ", evaluating: " + notificationsToEvaluate.size() ); 
				}
			}
			
			if( count > 0 && isAllPredicatesPassed(notificationsToEvaluate) ) {
				try {
					Delivery current = getCurrentDelivery();
					current.setTime( new DateTime() );
					current.setCount( count );
					
					Collection<Notification> msgs = current.getNotifications();
					for( Closure c : filters ) {
						msgs = (Collection<Notification>)call( c, msgs );
					}
					
					if( LOGGER.isInfoEnabled() ) { LOGGER.info( "processDelivery: delivering " + msgs.size() + " notifications from: " + current ); }
					
					for( Closure receiver : receivers ) {
						try {
                            call(receiver, msgs);
						}
						catch( Exception ex ) {
							LOGGER.error( "Error raised from notification receiver", ex );
						}
					}
				}
				finally {
					archiveDelivery();
				}
			}
		}
		catch( Exception ex ) {
			LOGGER.error( "Error delivering notifications", ex );
		}
	}

	private void archiveDelivery() {
		Delivery previous = getPreviousDelivery();
		if( previous != null ) {
			previous.setNotifications( null ); // Only retain the actual notifications for the current and previous
		}
		deliveries.addFirst(new Delivery());

		// Drop deliveries beyond the rentention window
		DateTime oldest = getDateInPast( maxDeliveryRetentionMinutes );
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "archiveDelivery: oldest notification to keep of " + deliveries.size() + " deliveries: " +  oldest); }
		
		while( true ) {
			Delivery del = deliveries.getLast();
			DateTime time = del.getTime();
			if( time == null || time.isAfter(oldest) ) {
				break;
			}
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "archiveDelivery: discarding delivery: " + del ); }
			deliveries.removeLast();
		}
		if( LOGGER.isDebugEnabled() ) {
			LOGGER.debug( "archiveDelivery: there are " + deliveries.size() + " remaining" );
		}
	}
	
	private void enforceNotificationLimit() {
		LinkedList<Notification> undelivered = getUndeliveredNotifications();
		while( undelivered.size() > maxNotifications ) {
			Notification notification = undelivered.removeFirst();
			LOGGER.warn( "enforceNotificationLimit: exceeded notification queue limit - removing: " + notification );
		}
	}
	
	private boolean isAllPredicatesPassed( List<Notification> notificationsToEvaluate ) throws Exception {
		for( ParameterizedRunnableWithResult<Boolean,List<Notification>> predicate : predicates ) {
			if( !predicate.run(notificationsToEvaluate) ) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isPassed(Closure closure, List<Notification> notificationsToEvaluate, boolean all) throws Exception {
		return all ? isAllNotificationsPassed(closure) : isAnyNotificationPassed(closure, notificationsToEvaluate);
	}
	
	private boolean isAnyNotificationPassed(Closure closure, List<Notification> notificationsToEvaluate) throws Exception {
		for( Notification notification : notificationsToEvaluate ) {
			if( isNotificationPassed(closure, notification) ) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAllNotificationsPassed(Closure closure) throws Exception {
		for( Notification notification : getUndeliveredNotifications() ) {
			if( !isNotificationPassed(closure, notification) ) {
				return false;
			}
		}
		return true;
	}

	private boolean isNotificationPassed(Closure closure, Notification notification) throws Exception {
		return coerceToBoolean( closure != null ? call(closure, notification) : false );
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
		return past.minusMinutes( minutes );
	}

    private Object call( Closure closure, Object param ) {
        int max = closure.getMaximumNumberOfParameters();
        if( max == 0 )  {
            return closure.call();
        }
        else if( max == 1 ) {
            return closure.call( param );
        }
        else if( max == 2 ) {
            return closure.call( param, this );
        }
        else {
            throw new RuntimeException( "Invalid number of arguments for closure" );
        }
    }

	synchronized private void start() {
        if( scheduler == null ) {
            scheduler = new Thread( new Runnable() {
               public void run() {
                   while( true ) {
                       try {
                           Thread.currentThread().sleep(60 * 1000);
                           processDelivery(getUndeliveredNotifications());
                       }
                       catch (Exception ex) {
                           LOGGER.error("start: failed sending notifications", ex);
                       }
                   }
               }
            } );
		}
	}
	
    public static boolean coerceToBoolean( Object obj ) {
        if( obj != null ) {
            if( obj instanceof Boolean ) {
                return ((Boolean)obj).booleanValue();
            }
            else if( obj instanceof Matcher) {
                return ((Matcher)obj).matches();
            }
            else {
                return Boolean.valueOf( String.valueOf(obj) ).booleanValue();
            }
        }
        return false;
    }

}

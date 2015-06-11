package org.scriptbox.selenium.ext.event;

import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBus {

	private static final Logger LOGGER = LoggerFactory.getLogger( EventBus.class );
	
	private ExecutorService executor;
	private Map<Object,Set<Closure>> listeners =  new HashMap<Object,Set<Closure>>();
	
	public EventBus( ) {
		executor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.HOURS,
			new ArrayBlockingQueue<Runnable>(10000, true), new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	synchronized public void subscribe( Object event, Closure closure ) {
		// Never update the listener set so no synchronization is required when firing event
		Set<Closure> set = listeners.get( event );
		set = set == null ?  new HashSet<Closure>() : new HashSet<Closure>(set);
		set.add( closure );
		listeners.put( event, set );
	}
	
	synchronized public void unsubscribe( Object event, Closure listener ) {
		// Never update the listener set so no synchronization is required when firing event
		Set<Closure> set = listeners.get( event );
		if( set != null && set.contains(listener) ) {
			set = new HashSet<Closure>(set);
			set.remove( listener );
			listeners.put( event, set );
		}
	}
	
	public void raise( final Object event, final Object... args ) {
		Set<Closure> lset  = null;
		synchronized( this ) {
			lset = listeners.get( event );
		}
		if( lset != null && lset.size() > 0 ) {
			fireEvent( lset, event, args );
		}
	}
	
	private void fireEvent( final Set<Closure> lset, final Object event, final Object... args ) {
		executor.execute( new Runnable() {
			public void run() {
                for( Closure listener : lset ) {
                    try {
                        int max = listener.getMaximumNumberOfParameters();
                        if (max == 0) {
                            listener.call();
                        }
                        else if ( max == 1 ) {
                            listener.call( args );
                        }
                        else if( max == 2 ) {
                            listener.call( args, event );
                        }
                        else {
                            throw new RuntimeException( "Invalid number of parameters for event listener: " + listener );
                        }
                    }
                    catch( Exception ex ) {
                        LOGGER.error( "Failed sending event: " + event + ", args: " + args + ", listener: " + listener, ex );
                    }
                }
            }
		});
	}
}

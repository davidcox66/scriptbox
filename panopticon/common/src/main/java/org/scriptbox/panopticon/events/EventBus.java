package org.scriptbox.panopticon.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.controls.BoxServiceListener;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBus implements BoxServiceListener {

	private static final Logger LOGGER = LoggerFactory.getLogger( EventBus.class );
	
	private BoxContext context;
	private ExecutorService executor;
	private Map<Object,Set<ParameterizedRunnable<Object>>> listeners = 
		new HashMap<Object,Set<ParameterizedRunnable<Object>>>();
	
	public EventBus( BoxContext context ) {
		this.context = context;
		executor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.HOURS,
			new ArrayBlockingQueue<Runnable>(10000, true), new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	public void starting(List arguments) throws Exception {
	}
	public void shutdown() throws Exception {
		executor.shutdown();
	}
	public void stopping() throws Exception {
	}
	
	synchronized public void subscribe( Object event, ParameterizedRunnable<Object> listener ) {
		// Never update the listener set so no synchronization is required when firing event
		Set<ParameterizedRunnable<Object>> set = listeners.get( event );
		set = set == null ? 
			new HashSet<ParameterizedRunnable<Object>>() :
			new HashSet<ParameterizedRunnable<Object>>(set); 
		set.add( listener );
		listeners.put( event, set );
	}
	
	synchronized public void unsubscribe( Object event, ParameterizedRunnable<Object> listener ) {
		// Never update the listener set so no synchronization is required when firing event
		Set<ParameterizedRunnable<Object>> set = listeners.get( event );
		if( set != null && set.contains(listener) ) {
			set = new HashSet<ParameterizedRunnable<Object>>(set); 
			set.remove( listener );
			listeners.put( event, set );
		}
	}
	
	public void raise( final Object... args ) {
		if( args.length == 0 ) {
			throw new RuntimeException( "Must call raise(event) with an event argument");
		}
		
		Set<ParameterizedRunnable<Object>> lset  = null;
		synchronized( this ) {
			lset = listeners.get( args[0] );
		}
		if( lset != null && lset.size() > 0 ) {
			fireEvent( lset, args );
		}
	}
	
	private void fireEvent( final Set<ParameterizedRunnable<Object>> lset, final Object... args ) {
		executor.execute( new Runnable() {
			public void run() {
				try {
					BoxContext.with( context, new ParameterizedRunnable<BoxContext>() {
						public void run( BoxContext bc ) {
							for( ParameterizedRunnable<Object> listener : lset ) {
								try {
									listener.run( args );
								}
								catch( Exception ex ) {
									LOGGER.error( "Failed sending event: " + args[0] + ", listener: " + listener, ex );
								}
							}
						}
					} );
				}
				catch( Exception ex ) {
					LOGGER.error( "Failed sending event: " + args[0], ex );
				}
			} 
		});
	}
}

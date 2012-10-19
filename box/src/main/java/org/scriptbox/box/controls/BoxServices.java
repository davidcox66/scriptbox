package org.scriptbox.box.controls;

import java.util.Collection;
import java.util.List;

import org.scriptbox.util.common.collection.Iterators;
import org.scriptbox.util.common.error.Errors;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxServices {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( BoxServices.class );
	
	public static Errors<BoxService> start( final Collection<BoxService> services, final List arguments, final Collection<BoxServiceListener> listeners ) throws Exception {
		Iterators.callAndCollectExceptions( listeners, new ParameterizedRunnable<BoxServiceListener>() {
			public void run( BoxServiceListener listener ) throws Exception {
				listener.starting( arguments );
			}
		} ).raiseException( "Error notifying listeners of start");
		
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "start: service=" + service + ", arguments=" + arguments ); }
				service.start( arguments );
			}
		} );
	}
	
	public static Errors<BoxService> stop( final Collection<BoxService> services, final Collection<BoxServiceListener> listeners ) throws Exception {
		Iterators.callAndCollectExceptions( listeners, new ParameterizedRunnable<BoxServiceListener>() {
			public void run( BoxServiceListener listener ) throws Exception {
				listener.stopping();
			}
		} ).logError( "Error notifying listeners of start", LOGGER);
		
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stop: service=" + service ); }
				service.stop();
			}
		} );
	}
	public static Errors<BoxService> shutdown( final Collection<BoxService> services, final Collection<BoxServiceListener> listeners ) throws Exception {
		Iterators.callAndCollectExceptions( listeners, new ParameterizedRunnable<BoxServiceListener>() {
			public void run( BoxServiceListener listener ) throws Exception {
				listener.stopping();
			}
		} ).logError( "Error notifying listeners of start", LOGGER);
		
		if( listeners != null && listeners.size() > 0 ) {
			for( BoxServiceListener listener : listeners ) {
				try {
					listener.shutdown();
				}
				catch( Exception ex ) {
					LOGGER.error( "callListeners: error notifying BoxServiceListener of shutdown", ex );
				}
			}
		}
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stop: service=" + service ); }
				service.shutdown();
			}
		} );
	}
}

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
	
	public static Errors<BoxService> start( Collection<BoxService> services, final List arguments ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "start: service=" + service + ", arguments=" + arguments ); }
				service.start( arguments );
			}
		} );
	}
	
	public static Errors<BoxService> stop( Collection<BoxService> services ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stop: service=" + service ); }
				service.stop();
			}
		} );
	}
	public static Errors<BoxService> shutdown( Collection<BoxService> services ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stop: service=" + service ); }
				service.shutdown();
			}
		} );
	}
}

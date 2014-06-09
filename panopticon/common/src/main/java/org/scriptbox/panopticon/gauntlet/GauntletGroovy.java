package org.scriptbox.panopticon.gauntlet;

import groovy.lang.Closure;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GauntletGroovy extends Gauntlet {

	private static final Logger LOGGER = LoggerFactory.getLogger( GauntletGroovy.class );
	
	public GauntletGroovy( BoxContext context, boolean immediate ) {
		super( context, immediate );
	}

	public void deliver( final Closure closure ) {
		super.deliver( new ParameterizedRunnable<Object[]>() {
			public void run( Object[] args ) {
				call( closure, args );
			}
		});
	}
	
	public void backoff( int minutes, final Closure closure ) throws Exception {
		backoff( minutes, minutes, closure );
	}
	
	public void backoff( int initial, int increment, final Closure closure ) throws Exception {
		super.backoff( initial, increment, new ParameterizedRunnableWithResult<Boolean,Object[]>() {
			public Boolean run( Object[] args ) throws Exception {
				return Closures.coerceToBoolean( call(closure,args) );
			}
		} );
	}
	
	public void between( String start, String end, final Closure closure ) throws Exception {
		between( start, end, false, closure );
	}
	
	public void between( String start, String end, boolean all, final Closure closure ) throws Exception {
		super.blackout( start, end, all, new ParameterizedRunnableWithResult<Boolean,Object[]>() {
			public Boolean run( Object[] args ) throws Exception {
				return Closures.coerceToBoolean( call(closure,args) );
			}
		} );
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
		if( closure != null ) {
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
		return null;
			
	}
}

package org.scriptbox.box.groovy;

import groovy.lang.Closure;

import java.util.regex.Matcher;

import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;

public class Closures {

	public static boolean coerceToBoolean( Object obj ) {
		if( obj != null ) {
			if( obj instanceof Boolean ) {
				return ((Boolean)obj).booleanValue();
			}
			else if( obj instanceof Matcher ) {
				return ((Matcher)obj).matches();
			}
			else {
				return Boolean.valueOf( String.valueOf(obj) ).booleanValue();
			}
		}
		return false;
	}
	
	public static <ARG> ParameterizedRunnable<ARG> toRunnable( final Closure closure, Class<ARG> clazz )  {
		if( closure != null ) {
			return new ParameterizedRunnable<ARG>() {
				public void run( ARG arg ) {
					closure.call( arg );
				}
			};
		}
		return null;
	}
	
	public static <RET,ARG> ParameterizedRunnableWithResult<RET,ARG> toRunnableWithResult( final Closure closure, Class<RET> retCls, Class<ARG> argCls )  {
		if( closure != null ) {
			return new ParameterizedRunnableWithResult<RET,ARG>() {
				public RET run( ARG arg ) {
					return (RET)closure.call( arg );
				}
			};
		}
		return null;
	}
	public static <ARG> ParameterizedRunnableWithResult<Boolean,ARG> toRunnableWithBoolean( final Closure closure, Class<ARG> argCls )  {
		if( closure != null ) {
			return new ParameterizedRunnableWithResult<Boolean,ARG>() {
				public Boolean run( ARG arg ) {
					return coerceToBoolean( closure.call(arg) );
				}
			};
		}
		return null;
	}
	
}

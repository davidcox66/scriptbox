package org.scriptbox.box.groovy;

import groovy.lang.Closure;

import java.util.regex.Matcher;

import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.obj.RunnableWithException;
import org.scriptbox.util.common.obj.RunnableWithThrowable;

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
	
	public static int coerceToInteger( Object obj ) {
		if( obj != null ) {
			if( obj instanceof Number ) {
				return ((Number)obj).intValue();
			}
		}
		return 0;
	}

	public static Runnable toRunnableWithDelegate( final Closure closure, Object delegate )  {
		if( closure != null ) {
			closure.setDelegate( delegate );
			return toRunnable( closure );
		}
		return null;
	}
	
	public static Runnable toRunnable( final Closure closure )  {
		if( closure != null ) {
			return new Runnable() {
				public void run() {
					closure.call();
				}
			};
		}
		return null;
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
	
	public static <RET,ARG> ParameterizedRunnableWithResult<RET,ARG> toRunnable( final Closure closure, Class<RET> retCls, Class<ARG> argCls )  {
		if( closure != null ) {
			return new ParameterizedRunnableWithResult<RET,ARG>() {
				public RET run( ARG arg ) {
					return (RET)(closure.getMaximumNumberOfParameters() > 0 ? closure.call(arg) : closure.call());    
				}
			};
		}
		return null;
	}
	public static <ARG> ParameterizedRunnableWithResult<Boolean,ARG> toRunnableWithBoolean( final Closure closure, Class<ARG> argCls )  {
		if( closure != null ) {
			return new ParameterizedRunnableWithResult<Boolean,ARG>() {
				public Boolean run( ARG arg ) {
					return coerceToBoolean(closure.getMaximumNumberOfParameters() > 0 ? closure.call(arg) : closure.call());    
				}
			};
		}
		return null;
	}
	
	public static <ARG> ParameterizedRunnableWithResult<Integer,ARG> toRunnableWithInteger( final Closure closure, Class<ARG> argCls )  {
		if( closure != null ) {
			return new ParameterizedRunnableWithResult<Integer,ARG>() {
				public Integer run( ARG arg ) {
					return coerceToInteger(closure.getMaximumNumberOfParameters() > 0 ? closure.call(arg) : closure.call());    
				}
			};
		}
		return null;
	}
	
	public static void callInClassLoader( ClassLoader loader, Runnable closure ) {
        if( closure != null ) {
            Thread thread = Thread.currentThread();
            ClassLoader current = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader( loader );
                closure.run();
            }
            finally {
               thread.setContextClassLoader( current );
            }
        }
	}
	public static void callInClassLoader( ClassLoader loader, RunnableWithException closure ) throws Exception {
        if( closure != null ) {
            Thread thread = Thread.currentThread();
            ClassLoader current = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader( loader );
                closure.run();
            }
            finally {
               thread.setContextClassLoader( current );
            }
        }
    }    
	public static void callInClassLoader( ClassLoader loader, RunnableWithThrowable closure ) throws Throwable {
        if( closure != null ) {
            Thread thread = Thread.currentThread();
            ClassLoader current = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader( loader );
                closure.run();
            }
            finally {
               thread.setContextClassLoader( current );
            }
        }
    }    
}

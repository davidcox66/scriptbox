package org.scriptbox.util.common.error;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

public class Errors<X> {

	private Map<X,Throwable> map;

	public Errors() {
		map = new HashMap<X,Throwable>();
	}
	
	public Errors( Map<X,Throwable> map ) {
		this.map = map;
	}
	
	public Errors<X> addAll( Errors<X> other ) {
		if( other != null ) {
			map.putAll( other.map );
		}
		return this;
	}

	public Errors<X> logDebug( String message, Logger logger ) {
		if( isAnyErrors() && logger.isDebugEnabled() ) {
			logger.debug( message + "\n" + exceptionsToString(), new Exception() );
		}
		return this;
	}
	public Errors<X> logInfo( String message, Logger logger ) {
		if( isAnyErrors() && logger.isInfoEnabled() ) {
			logger.info( message + "\n" + exceptionsToString(), new Exception() );
		}
		return this;
	}
	public Errors<X> logWarn( String message, Logger logger ) {
		if( isAnyErrors() && logger.isDebugEnabled() ) {
			logger.warn( message + "\n" + exceptionsToString(), new Exception() );
		}
		return this;
	}
	public Errors<X> logError( String message, Logger logger ) {
		if( isAnyErrors() && logger.isErrorEnabled() ) {
			logger.error( message + "\n" + exceptionsToString(), new Exception() );
		}
		return this;
	}
	
	public boolean isAnyErrors() {
		return map.size() > 0;
	}
	
	public void raiseRuntimeException( String msg ) {
		if( isAnyErrors() ) {
			throw new RuntimeException( msg + "\n" + exceptionsToString() );
		}
	}
	public void raiseException( String msg ) throws Exception {
		if( isAnyErrors() ) {
			throw new Exception( msg + "\n" + exceptionsToString() );
		}
	}
	public <E extends Exception> void raiseException( String msg, Class<E> cls ) throws Exception {
		if( isAnyErrors() ) {
			Constructor<E> ctor = cls.getConstructor( String.class );
			throw ctor.newInstance( msg + "\n" + exceptionsToString() );
		}
	}
	public void raiseExceptionWithCauses( String msg ) throws ExceptionWithCauses {
		if( isAnyErrors() ) {
			Map<String,String> causes = new HashMap<String,String>();
			for( Map.Entry<X,Throwable> entry : map.entrySet() ) {
				causes.put( entry.getKey().toString(), ExceptionHelper.toString(entry.getValue()) );
			}
			throw new ExceptionWithCauses( msg, causes );
		}
	}
	
    public String exceptionsToString() {
    	StringBuilder builder = new StringBuilder();
    	for( Map.Entry<X,Throwable> entry : map.entrySet() ) {
    		if( builder.length() > 0 ) {
    			builder.append( "\n" );
    		}
    		builder.append( entry.getKey() + " : " + ExceptionHelper.toString(entry.getValue()) );
    	}
    	return builder.toString();
    }
    
}

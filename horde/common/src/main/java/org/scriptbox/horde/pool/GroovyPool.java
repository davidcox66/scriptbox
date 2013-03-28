package org.scriptbox.horde.pool;

import groovy.lang.Closure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class GroovyPool {

	protected GenericObjectPool pool;
	protected PoolableObjectFactory factory;
	
	private static Set<String> PARAMS = new HashSet<String>(
		Arrays.asList( new String[] { 
		"makeObject",
		"destroyObject",
		"validateObject",
		"activateObject",
		"passivateObject",
		"maxActive"} )
	);
	
	public GroovyPool( final Map<String,Object> params ) {
		
		for( String param : params.keySet() ) {
			if( !PARAMS.contains(param) ) {
				throw new IllegalArgumentException( "Unknown pool parameter: '" + param + "'" ); 
			}
		}
		
		final Closure cMakeObject = (Closure)params.get( "makeObject" );
		final Closure cDestroyObject = (Closure)params.get( "destroyObject" );
		final Closure cValidateObject = (Closure)params.get( "validateObject" );
		final Closure cActivateObject = (Closure)params.get( "activateObject" );
		final Closure cPassivateObject = (Closure)params.get( "passivateObject" );

		final Integer maxActive = (Integer)params.get( "maxActive" );
		
		if( cMakeObject == null ) {
			throw new IllegalArgumentException( "Must define a pool with makeObject()" );
		}
		
		factory = new PoolableObjectFactory() {
			  public Object makeObject() throws Exception {
				  return cMakeObject.call();
			  }
			  public void destroyObject(Object obj) throws Exception {
				 if( cDestroyObject != null ) { 
					 cDestroyObject.call( obj );
				 }
			  }

			  public boolean validateObject(Object obj) {
				 if( cValidateObject != null ) { 
					 return (Boolean)cValidateObject.call( obj );
				 }
				 return true;
			  }

			  public void activateObject(Object obj) throws Exception {
				 if( cActivateObject != null ) {
					 cActivateObject.call( obj );
				 }
			  }

			  public void passivateObject(Object obj) throws Exception {
				 if( cPassivateObject != null ) {
					 cPassivateObject.call( obj );
				 }
			  }
		};
		if( maxActive != null ) {
			pool = new GenericObjectPool( factory, maxActive );
		}
		else {
			pool = new GenericObjectPool( factory );
		}
	}

	public void clear() throws Exception {
		pool.clear();
	}
	
	public void close() throws Exception {
		pool.close();
	}
	public Object borrowObject() throws Exception {
		return pool.borrowObject();
	}
	
	public void returnObject(Object borrowed) throws Exception {
		pool.returnObject( borrowed );
	}
	
}

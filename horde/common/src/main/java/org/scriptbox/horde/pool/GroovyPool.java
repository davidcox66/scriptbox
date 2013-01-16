package org.scriptbox.horde.pool;

import groovy.lang.Closure;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class GroovyPool {

	protected GenericObjectPool pool;
	protected PoolableObjectFactory factory;
	
	public GroovyPool( final Map<String,Object> params ) {
		
		final Closure cMakeObject = (Closure)params.get( "makeObject" );
		final Closure cDestroyObject = (Closure)params.get( "destroyObject" );
		final Closure cValidateObject = (Closure)params.get( "validateObject" );
		final Closure cActivateObject = (Closure)params.get( "activateObject" );
		final Closure cPassivateObject = (Closure)params.get( "passivateObject" );

		final Integer maxActive = (Integer)params.get( "maxActive" );
		
		if( cMakeObject == null ) {
			throw new RuntimeException( "Must define a pool with makeObject()" );
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
				 if( cDestroyObject != null ) { 
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

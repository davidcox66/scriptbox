package org.scriptbox.horde.http;

import groovy.lang.Closure;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovyHttp extends Http {

	private static final Logger LOGGER = LoggerFactory.getLogger( GroovyHttp.class );
	
	public void getAndRead( String url, Closure closure  ) throws ClientProtocolException, IOException {
		HttpResponse response = get( url );
		HttpEntity entity = response.getEntity();
		if( entity != null ) {
			InputStream input = null;
			try {
				input = entity.getContent();
				closure.call( input );
			}
			finally {
				try {
					if( input != null ) {
						input.close();
					}
				}
				catch( Exception ex ) {
					LOGGER.error( "Error closing HTTP input stream", ex );
				}
				EntityUtils.consume(entity);	
			}
		}
	}
	
	public void getAndReadLine( String url, Closure closure ) throws ClientProtocolException, IOException {
		HttpResponse response = get( url );
		HttpEntity entity = response.getEntity();
		if( entity != null ) {
			BufferedReader input = null;
			try {
				input = new BufferedReader( new InputStreamReader(entity.getContent()) );
				String line = null;
				while( (line = input.readLine()) != null ) {
					closure.call( line );
				}
			}
			finally {
				try {
					if( input != null ) {
						input.close();
					}
				}
				catch( Exception ex ) {
					LOGGER.error( "Error closing HTTP input stream", ex );
				}
				EntityUtils.consume(entity);	
			}
		}
	}
}

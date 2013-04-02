package org.scriptbox.panopticon.apache;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.box.controls.BoxServiceListener;
import org.scriptbox.util.common.io.TailReader;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheLogParser implements BoxServiceListener {

	private static final Logger LOGGER = LoggerFactory.getLogger( ApacheLogParser.class );

	private static final int TAIL_INTERVAL = 2000;
	  
	private File file;
	private ParameterizedRunnableWithResult<ApacheLogEntry,String> parser;
	private TailReader reader;	
	  
	private Map<String,ApacheLogTotal> totals = new HashMap<String,ApacheLogTotal>();

	ApacheLogParser( String location, ParameterizedRunnableWithResult<ApacheLogEntry,String> parser ) {
	    this.file = new File(location);
	    this.parser = parser;
	}
	
	public void sample( ParameterizedRunnable<ApacheLogTotal> closure ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "execute: collecting apache log metrics"); }
	      synchronized( totals ) {
	    	  for( ApacheLogTotal lt : totals.values() ) {
	    		  closure.run( lt );
	    		  lt.reset();
	    	  }
	      }     
	  }

	  public void starting( List arguments ) throws Exception {
		  if( reader == null ) {
			  createReader();
		  }
	  }
	  
	  public void stopping() throws Exception {
	  }

	  public void shutdown() {
		  if( LOGGER.isInfoEnabled() ) { LOGGER.info( "shutdown: stopping tail of: " + file ); }
		  if( reader != null ) {
		      reader.stop();    
		  }
	  }
	  
	  private void createReader() {
	      if( file.exists() ) {
	          if( LOGGER.isInfoEnabled() ) { LOGGER.info( "createReader: starting tail of " + file ); }
		      reader = new TailReader();
		      reader.tail( file, TAIL_INTERVAL, new ParameterizedRunnable<String>() {
		    	  public void run( String line ) { 
		    		  try {
				          ApacheLogEntry entry = parser.run(line);    
				          if( entry != null ) {
			                  if( LOGGER.isTraceEnabled() ) { LOGGER.trace( "reader: entry=" + entry); }
				              synchronized( totals ) {
				                  ApacheLogTotal lt = totals.get( entry.url );
				                  if( lt == null ) {
				                      lt = new ApacheLogTotal( entry.url );
				                      totals.put( entry.url, lt );
				                  }
				                  lt.min = Math.min( lt.min, entry.responseTime );
				                  lt.max = Math.max( lt.max, entry.responseTime );
				                  lt.totalResponseTime += entry.responseTime;
				                  lt.totalRequests++;
				              }
				          }
			              else {
			                  if( LOGGER.isInfoEnabled() ) { LOGGER.info( "reader: ignoring line: " + line); }
			              }
		    		  }
		    		  catch( Exception ex ) {
		    			  LOGGER.warn( "Error parsing log line: " + line, ex );
		    		  }
			      }
		      } );
	      }
	      else {
	          LOGGER.warn( "createReader: Apache log does not exists yet: " + file );
	      }
	  }

}

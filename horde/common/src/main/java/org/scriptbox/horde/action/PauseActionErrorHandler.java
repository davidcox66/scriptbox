package org.scriptbox.horde.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseActionErrorHandler implements ActionErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger( PauseActionErrorHandler.class );
	private long millis;
	
	public PauseActionErrorHandler( long millis ) {
		if( millis <= 0 ) {
			throw new IllegalArgumentException( "Pause cannot be <= 0");
		}
		this.millis = millis;
	}
	
	@Override
	public void handle(Throwable ex) throws Throwable {
		Thread current = Thread.currentThread();
		LOGGER.error( "Error from load test, pausing momentarily: " + current.getName() + " ...", ex ); 
		Thread.sleep( millis );
		LOGGER.info( "Resuming load test: " + current.getName() ); 
	}

}

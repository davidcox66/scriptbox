package org.scriptbox.util.common.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.input.Tailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiTailer {

	private static final Logger LOGGER = LoggerFactory.getLogger( MultiTailer.class );
	
    private FileSource source;
    private int interval;
    private boolean running;
    
    private Map<File,Tailer> tailers = new HashMap<File,Tailer>();
    private TailerListenerFactory factory;
    
    public MultiTailer( int interval, FileSource source, TailerListenerFactory factory ) {
    	this.interval = interval;
    	this.source = source;
    	this.factory = factory;
    }
    
    public void stop () {
    	if( running ) {
	    	running = false;
	    	for( Tailer tailer : tailers.values() ) {
	    		tailer.stop();
	    	}
	    	tailers.clear();
    	}
    }
  
    public void start() throws IOException {
    	if( !running ) {
	    	running = true;
	    	
	    	processFileSource();
			Thread thread = new Thread(new Runnable() {
				public void run() {
					while( running ) {
						try {
							processFileSource();
							Thread.sleep( interval );
						}
						catch( Exception ex ) {
							LOGGER.error( "Error checking for file changes", ex );
						}
					}
				}
			});
			thread.setDaemon(true);
			thread.start();    	
    	}
    }

    private void processFileSource() throws IOException {
    	Set<File> files = source.getFiles();
    	closeOldTailers( files );
    	openNewTailers( files );
    }
    
    synchronized private void closeOldTailers( Set<File> files ) {
    	Set<File> oldFiles = new HashSet<File>(tailers.keySet());
    	oldFiles.removeAll(files);
    		  
    	if( oldFiles.size() > 0 ) {
    		for( File oldFile : oldFiles ) {
				LOGGER.info( "closeOldTailers: closing: '" + oldFile + "'" );
    			Tailer tailer = tailers.remove( oldFile );
    			tailer.stop();
    		}
    	}
    }

    synchronized private void openNewTailers( Set<File> files ) throws IOException {
    	Set<File> newFiles = new HashSet<File>(files);
    	newFiles.removeAll( tailers.keySet() );
    
    	if( newFiles.size() > 0 ) {
    		for( File newFile : newFiles ) {
    			if( newFile.exists() ) {
    				LOGGER.info( "openNewTailers: opening: '" + newFile + "'" );
    				Tailer tailer = Tailer.create( newFile, factory.create(newFile), interval, true );
    				tailers.put( newFile, tailer );
    			}
    		}
    	}
    }
}

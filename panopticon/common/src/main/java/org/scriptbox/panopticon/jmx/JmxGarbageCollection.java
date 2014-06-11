package org.scriptbox.panopticon.jmx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxRmiConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxGarbageCollection implements ExecRunnable {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxGarbageCollection.class );
	
    private boolean deltas;
    private int maxSize;
    private int maxSeconds;
    private List<GarbageCollector> collectors = null;
    private Map<GarbageCollector,Historical<GarbageCollection>> histories = new HashMap<GarbageCollector,Historical<GarbageCollection>>();
    private ParameterizedRunnable<Object> runnable;
    private boolean connected=true;
    
    public JmxGarbageCollection( boolean deltas, ParameterizedRunnable<Object> runnable ) {
    	this( deltas, 1, 0, runnable );
    }
    
    public JmxGarbageCollection( boolean deltas, int maxSize, int maxSeconds, ParameterizedRunnable<Object> runnable ) {
    	this.deltas = deltas;
    	this.maxSize = maxSize;
    	this.maxSeconds = maxSeconds;
    	this.runnable = runnable;
    }
    
    public void run() {
		JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
		try {
	        for( GarbageCollector coll : getCollectors() ) {
	        	GarbageCollection info = coll.getInfo( connection );
        		Historical<GarbageCollection> hist = histories.get( coll );
        		if( hist == null ) {
        			hist = new Historical<GarbageCollection>(maxSize,maxSeconds);
        			histories.put( coll, hist );
        		}
        		Historical.Sample<GarbageCollection> last = hist.getLatest();
	        	if( !deltas || last == null || !last.getData().equals(info) ) {
	        		hist.add( info );
	        		if( maxSize == 1 && maxSeconds == 0 ) {
	        			runnable.run( info );
	        		}
	        		else {
	        			runnable.run( hist );
	        		}
	        	}
        		if( LOGGER.isDebugEnabled() ) {
	        		LOGGER.debug( "JmxGarbageCollection: latest:" + info + ", previous: " + (last != null ? last.getData() : null) );
        		}
	        }
	        connected = true;
		}
		catch( Exception ex ) {
			if( connected ) {
				if( JmxRmiConnection.isConnectException(ex) ) {
					connected = false;
					LOGGER.warn( "Could not connect to get GC information: " + connection, ex );
				}
				else {
					LOGGER.error( "Failed getting GC information", ex );
				}
			}
		}
    }
 
    synchronized private List<GarbageCollector> getCollectors() throws Exception {
    	if( collectors == null ) {
			JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
			collectors = GarbageCollector.getCollectors( connection );
    	}
    	return collectors;
    }
    
}

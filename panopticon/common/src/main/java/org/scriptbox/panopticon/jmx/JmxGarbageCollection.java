package org.scriptbox.panopticon.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxGarbageCollection implements ExecRunnable {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxGarbageCollection.class );
	
	private boolean deltas;
    private List<GarbageCollector> collectors = null;
    private Map<GarbageCollector,GarbageCollection> infos = new HashMap<GarbageCollector,GarbageCollection>();
    
    private ParameterizedRunnable<GarbageCollection[]> runnable;
    
    public JmxGarbageCollection( boolean deltas, ParameterizedRunnable<GarbageCollection[]> runnable ) {
    	this.deltas = deltas;
    	if( deltas ) {
    		infos = new HashMap<GarbageCollector,GarbageCollection>();
    	}
    	this.runnable = runnable;
    }
    
    public void run() {
		JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
		try {
	        for( GarbageCollector coll : getCollectors() ) {
	        	GarbageCollection info = coll.getInfo( connection );
        		GarbageCollection last = infos.get( coll );
	        	if( !deltas || last == null || !last.equals(info) ) {
	        		infos.put( coll, info );
	        		GarbageCollection[] args = new GarbageCollection[2];
	        		args[0] = info;
	        		args[1] = last;
        			runnable.run( args );
	        	}
	        }
		}
		catch( Exception ex ) {
			LOGGER.error( "Failed getting GC information", ex );
		}
    }
 
    synchronized private List<GarbageCollector> getCollectors() throws MalformedObjectNameException, InstanceNotFoundException, AttributeNotFoundException, ReflectionException, MBeanException, IOException {
    	if( collectors == null ) {
			JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
			collectors = GarbageCollector.getCollectors( connection );
    	}
    	return collectors;
    }
    
}

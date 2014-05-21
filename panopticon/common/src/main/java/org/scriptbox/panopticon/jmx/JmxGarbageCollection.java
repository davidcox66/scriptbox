package org.scriptbox.panopticon.jmx;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

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
	
    private List<GarbageCollector> collectors = null;

    private ParameterizedRunnable<GarbageCollector.Info> runnable;
    
    public JmxGarbageCollection( ParameterizedRunnable<GarbageCollector.Info> runnable ) {
    	this.runnable = runnable;
    }
    
    public void run() {
		JmxConnection connection = ExecContext.getEnclosing(JmxProcess.class).getConnection();
		try {
	        for( GarbageCollector coll : getCollectors() ) {
	        	GarbageCollector.Info info = coll.getInfo( connection );
	        	runnable.run( info );
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

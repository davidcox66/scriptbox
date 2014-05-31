package org.scriptbox.panopticon.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxHeap implements ExecRunnable {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxHeap.class );
	
    private boolean deltas;
    private int maxSize;
    private int maxSeconds;
    private Historical<Heap> history;
    
    private ParameterizedRunnable<Object> runnable;
    
    public JmxHeap( boolean deltas, ParameterizedRunnable<Object> runnable ) {
    	this( deltas, 1, 0, runnable );
    }
    
    public JmxHeap( boolean deltas, int maxSize, int maxSeconds, ParameterizedRunnable<Object> runnable ) {
    	this.deltas = deltas;
    	this.maxSize = maxSize;
    	this.maxSeconds = maxSeconds;
    	this.history = new Historical<Heap>(maxSize,maxSeconds);
    	this.runnable = runnable;
    }
    
    public void run() {
    	try {
	    	final JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
			final JmxConnection connection = proc.getConnection();
			
			Object o = connection.getAttribute(new ObjectName("java.lang:type=Memory"), "HeapMemoryUsage");
			CompositeData cd = (CompositeData)o;
			Heap mem = new Heap(
				((Number)cd.get("init")).longValue(),
				((Number)cd.get("committed")).longValue(),
				((Number)cd.get("max")).longValue(),
				((Number)cd.get("used")).longValue() );
				
    		Historical.Sample<Heap> last = history.getLatest();
        	if( !deltas || last == null || !last.getData().equals(mem) ) {
        		history.add( mem );
        		if( maxSize == 1 && maxSeconds == 0 ) {
        			runnable.run( mem );
        		}
        		else {
        			runnable.run( history );
        		}
	        }
		}
		catch( Exception ex ) {
			LOGGER.error( "Failed getting heap information", ex );
		}
    }
}

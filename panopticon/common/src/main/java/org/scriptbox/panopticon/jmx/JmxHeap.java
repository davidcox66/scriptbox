package org.scriptbox.panopticon.jmx;

import java.rmi.ConnectException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxRmiConnection;
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
    private boolean connected=true;
    
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
    	final JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
		final JmxConnection connection = proc.getConnection();
    	try {
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
        	connected = true;
		}
		catch( Exception ex ) {
			if( connected ) {
				if( JmxRmiConnection.isConnectException(ex) ) {
					connected = false;
					LOGGER.warn( "Could not connect to get heap information: " + connection, ex );
				}
				else {
					LOGGER.error( "Failed getting heap information", ex );
				}
			}
		}
    }
}

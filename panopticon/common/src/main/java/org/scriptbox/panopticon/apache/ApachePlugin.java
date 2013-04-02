package org.scriptbox.panopticon.apache;

import java.net.URL;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.ExecBlock;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.box.jmx.proc.GenericProcess;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApachePlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApachePlugin.class);

	private static GenericProcess APACHE = new GenericProcess("Apache");

	private CaptureStore store;

	public CaptureStore getStore() {
		return store;
	}

	public void setStore(CaptureStore store) {
		this.store = store;
	}

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(ApacheInjector.class));
		super.contextCreated(context);
	}
	
	public void status( final String url ) {
		ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		container.add(new ExecRunnable() {
			public void run() throws Exception {
				ApacheStatus status = ApacheStatusParser.parse( new URL(url) );    
			    store( "status", "totalAccesses", status.totalAccesses );
			    store( "status", "totalKbytes", status.totalKbytes );
			    store( "status", "cpuLoad", status.cpuLoad );
			    store( "status", "uptime", status.uptime );
			    store( "status", "requestsPerSecond", status.requestsPerSecond );
			    store( "status", "bytesPerSecond", status.bytesPerSecond );
			    store( "status", "bytesPerRequest", status.bytesPerRequest );
			    store( "status", "busyWorkers", status.busyWorkers );
			    store( "status", "idleWorkers", status.idleWorkers );
			      
			    store( "status", "waitingForConnection", status.waitingForConnection );
			    store( "status", "startingUp", status.startingUp );
			    store( "status", "readingRequest", status.readingRequest );
			    store( "status", "sendingReply", status.sendingReply );
			    store( "status", "keepAlive", status.keepAlive );
			    store( "status", "closingConnection", status.closingConnection );
			    store( "status", "logging", status.logging );
			    store( "status", "gracefullyFinishing", status.gracefullyFinishing );
			    store( "status", "idleCleanup", status.idleCleanup );
			    store( "status", "openSlot", status.openSlot );
			    store( "status", "unknown", status.unknown );
			}
		} );
	}
	
	public void balancer( String location ) {
		
	}
	public void log( final String location, final ParameterizedRunnableWithResult<ApacheLogEntry,String> closure ) {
		final ApacheLogParser parser = new ApacheLogParser(location,closure);
		ExecBlock<ExecRunnable> container = ExecContext.getEnclosing(ExecBlock.class);
		container.add(new ExecRunnable() {
			public void run() throws Exception {
				parser.sample( new ParameterizedRunnable<ApacheLogTotal>() {
					public void run( ApacheLogTotal lt ) { 
			              if( lt.totalRequests > 0 ) {
				              store( "log", lt.url + "(min)", lt.min );
				              store( "log", lt.url + "(max)", lt.max );
				              store( "log", lt.url + "(totalAccesses)", lt.totalRequests );
				              store( "log", lt.url + "(averageResponseTime)", lt.totalResponseTime / lt.totalRequests );
				              lt.reset();
			              }
			          } 
			      } );
			}
		} );
		// Add this as a BoxServiceListener to get notification of stopping and
		// shutdown
		BoxContext.getCurrentContext().getBeans().put(parser.toString(), parser);
	}
	public void reset( String processPattern ) {
	}
	
	private void store(String attr, String metric, double value) {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("store: attr=" + attr + ",, metric=" + metric + ", value=" + value );
			}
			store.store(new CaptureResult(APACHE, attr, metric, value));
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed storing: attr=" + attr + ", metric=" + metric + ", value=" + value );
		}
	}
}

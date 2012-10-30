package org.scriptbox.panopticon.jmx;

import java.util.Set;

import javax.management.Attribute;
import javax.management.ObjectName;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.panopticon.capture.CaptureContext;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.scriptbox.box.jmx.query.MBeanQuery;
import org.scriptbox.box.jmx.query.MBeanQueryHandler;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxCapture implements ExecRunnable {
	
	private static final Logger LOGGER = LoggerFactory .getLogger(JmxCapture.class);

	private MBeanQuery query;
	private ParameterizedRunnableWithResult<CaptureResult,CaptureContext> closure;

	public JmxCapture(
		Object oneOrMoreAttributes, 
		Object oneOrMoreExclusions,
		boolean caching, 
		ParameterizedRunnableWithResult<CaptureResult,CaptureContext> closure) 
	{
		query = new MBeanQuery( oneOrMoreAttributes, oneOrMoreExclusions, caching );
		this.closure = closure;
	}

	/**
	 * Iterates through each mbean defined in the enclosing query() element and
	 * gets its specified attributes.
	 */
	@Override
	public void run() throws Exception {
		final CaptureStore cs = BoxContext.getCurrentContext().getBeans().getEx("store", CaptureStore.class );
		final JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
		final JmxConnection connection = proc.getConnection();
		final Set<ObjectName> objectNames = ExecContext.getEnclosing(Set.class);
		query.query( connection, objectNames, new MBeanQueryHandler() {
			public void process( ObjectName objectName, Attribute attr ) throws Exception {
				capture(cs,connection,proc,objectName, attr);
			}
		} );
	}

	/**
	 * Consolidates some of the work involved with capturing a statistic. If
	 * there is a closure defined for this capture() element, then it will be
	 * called with the contextual and statstic data. The closure then returns
	 * may return a CaptureResult with the statistics in whatever form was
	 * necessary.
	 * 
	 * @param ctx
	 * @param objectName
	 * @param attr
	 */
	protected void capture(CaptureStore cs, JmxConnection connection, JmxProcess proc, ObjectName objectName, Attribute attr)  throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("storing attribute: " + objectName + "." + attr.getName() + "=" + attr.getValue() );
		}
		CaptureContext ctx = new CaptureContext(proc,connection, objectName, attr);
		CaptureResult result = null;
		if (closure != null) {
			result = closure.run(ctx);
			if( result != null && result.millis == 0 ) { // Ensure a proper timestamp if closure didn't bother to assign it
				result.millis = System.currentTimeMillis();
			}
		} 
		else {
			result = new CaptureResult(proc, objectName.toString(), attr.getName(), attr.getValue(), System.currentTimeMillis());
		}
		if (result != null ) {
			cs.store(result);
		}
	}
}

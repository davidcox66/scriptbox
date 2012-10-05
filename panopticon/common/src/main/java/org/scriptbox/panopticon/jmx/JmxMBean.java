package org.scriptbox.panopticon.jmx;

import java.util.Set;

import javax.management.ObjectName;

import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.plugins.jmx.MBeanProxy;
import org.scriptbox.util.common.obj.ParameterizedRunnable;

public class JmxMBean implements ExecRunnable {

	private ParameterizedRunnable<MBeanProxy> closure;
	
	public JmxMBean( ParameterizedRunnable<MBeanProxy> closure ) {
		this.closure = closure;
	}
	
	public void run() throws Exception {
		JmxConnection connection = ExecContext.getNearestEnclosing(JmxProcess.class).getConnection();
        Set<ObjectName> objectNames = ExecContext.getEnclosing( Set.class );
        for( ObjectName objectName : objectNames ) {
	        MBeanProxy proxy = new MBeanProxy( connection, objectName );
	        closure.run( proxy );
        }
	}
}

package org.scriptbox.box.jmx.proc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxConnections;
import org.scriptbox.box.jmx.vm.VmJmxUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.os.proc.ProcessStatus;
import org.scriptbox.util.common.os.proc.ProcessStatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

public class JmxVmProcessProvider extends JmxProcessProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxVmProcessProvider.class );

	private ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder;
	
	public JmxVmProcessProvider( JmxConnections connections, String name, ParameterizedRunnableWithResult<Boolean,ProcessStatus> finder ) {
		super( connections, name );
		this.finder = finder;
	}
	
	@Override
	public void run() throws Exception {
		Map<String,VirtualMachine> vms = VmJmxUtil.getVirtualMachines();
		List<ProcessStatus> procs = new ArrayList<ProcessStatus>();
		for( Map.Entry<String,VirtualMachine> entry : vms.entrySet() ) {
			int pid = 0;
			try { 
				pid = Integer.parseInt(entry.getKey());
			}
			catch( Exception ex ) {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "run: could not parse pid: '" + entry.getKey() + "'" ); }
			}
			VirtualMachine vm = entry.getValue();
			Properties props = vm.getAgentProperties();
			ProcessStatus ps = new ProcessStatus();
			ps.pid = pid; 
			ps.user = vm.getSystemProperties().getProperty("user.name");
			ps.command = props.getProperty("sun.jvm.args") + " " + props.getProperty("sun.java.command");
			if( finder.run(ps) ) {
				procs.add( ps );
			}
		}
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "run: matching processes=" + procs ); }
		for( ProcessStatus proc : procs ) {
			JmxConnection connection = getConnections().getConnection( proc.pid, proc.command );
			with( new JmxProcess(getName(),connection) );
		}
	}
}

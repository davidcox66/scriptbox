package org.scriptbox.box.jmx.vm;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxConnectionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class VmJmxConnectionBuilder implements JmxConnectionBuilder {

	private static Logger LOGGER = LoggerFactory.getLogger( VmJmxConnectionBuilder.class );
	
	@Override
	public JmxConnection getForPid(int pid, String command ) throws Exception {
		VirtualMachine vm = null;
		try { 
			vm = VirtualMachine.attach(String.valueOf(pid));
		}
		catch( Exception ex ) {
			throw new Exception( "Error attaching to VM: pid=" + pid + ", command=" + command, ex );
		}
		
		try {
			String connectorAddress = VmJmxUtil.getConnectorAddress( vm );
			if( LOGGER.isDebugEnabled() ) { 
				LOGGER.debug( "getForPid: " +
					"pid=" + pid + 
					", command=" + command + 
					", connectorAddress=" + connectorAddress +
					", agentProperties=" + vm.getAgentProperties() + 
					", systemProperties=" + vm.getSystemProperties() +
					", vm=" + vm); 
			}
			return new JmxConnection( connectorAddress );
		}
		finally {
			vm.detach();
		}
	}
}

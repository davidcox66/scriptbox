package org.scriptbox.box.plugins.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class VmJmxConnectionBuilder implements JmxConnectionBuilder {

	private static Logger LOGGER = LoggerFactory.getLogger( VmJmxConnectionBuilder.class );
	
	@Override
	public JmxConnection getForPid(int pid, String command ) throws Exception {
		VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
		try {
			String connectorAddress = VmJmxUtil.getConnectorAddress( vm );
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getForPid: pid=" + pid + ", command=" + command + ", connectorAddress=" + connectorAddress ); }
			return new JmxConnection( connectorAddress );
		}
		finally {
			vm.detach();
		}
	}
}

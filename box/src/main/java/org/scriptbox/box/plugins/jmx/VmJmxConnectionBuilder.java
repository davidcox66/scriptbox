package org.scriptbox.box.plugins.jmx;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.VirtualMachine;

@SuppressWarnings("restriction")
public class VmJmxConnectionBuilder implements JmxConnectionBuilder {

	private static Logger LOGGER = LoggerFactory.getLogger( VmJmxConnectionBuilder.class );
	
	private static final String CONNECTOR_ADDRESS_PROPERTY = "com.sun.management.jmxremote.localConnectorAddress";
	
	@Override
	public JmxConnection getForPid(int pid, String command ) throws Exception {
		VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
		try {
			Properties props = vm.getAgentProperties();
			String connectorAddress = props.getProperty(CONNECTOR_ADDRESS_PROPERTY);
			if (connectorAddress == null) {
				props = vm.getSystemProperties();
				String home = props.getProperty("java.home");
				String agent = home + File.separator + "lib" + File.separator + "management-agent.jar";
				vm.loadAgent(agent);
				props = vm.getAgentProperties();
				connectorAddress = props.getProperty(CONNECTOR_ADDRESS_PROPERTY);
		   }
		   if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getForPid: pid=" + pid + ", command=" + command + ", connectorAddress=" + connectorAddress ); }
		   return new JmxConnection( connectorAddress );
		}
		finally {
			vm.detach();
		}
	}

}

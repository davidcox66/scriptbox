package org.scriptbox.box.jmx.vm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

@SuppressWarnings("restriction")
public class VmJmxUtil {

	private static Logger LOGGER = LoggerFactory.getLogger( VmJmxUtil.class );
	
	private static final String CONNECTOR_ADDRESS_PROPERTY = "com.sun.management.jmxremote.localConnectorAddress";

	public static void main( String[] args ) {
		try {
			Map<String,String> vms = getVirtualMachineUrls();
			for( Map.Entry<String,String> entry : vms.entrySet() ) {
				System.out.println( entry.getKey() );
				System.out.println( entry.getValue() );
				System.out.println( "" );
			}
		}
		catch( Exception ex ) {
			LOGGER.error( "Error getting virtual machine list", ex );
		}
	}
	
	public static Map<String,VirtualMachine> getVirtualMachines() {
		Map<String, VirtualMachine> result = new HashMap<String, VirtualMachine>();
		List<VirtualMachineDescriptor> list = VirtualMachine.list();
		for (VirtualMachineDescriptor vmd: list) {
			String desc = vmd.toString();
			try {
				result.put(desc, VirtualMachine.attach(vmd));
			} 
			catch( IOException ex ) {
				LOGGER.error( "Error getting virtual machine", ex );
			} 
			catch( AttachNotSupportedException ex ) {
				LOGGER.error( "Error getting virtual machine", ex );
			}
		}
		return result;
	}

	public static Map<String,String> getVirtualMachineUrls() {
		Map<String,VirtualMachine> vms = getVirtualMachines();
		Map<String,String> ret = new HashMap<String,String>();
		for( Map.Entry<String,VirtualMachine> entry : vms.entrySet() ) {
			try {
				ret.put( entry.getKey(), getConnectorAddress(entry.getValue()) );
			}
			catch( Exception ex ) {
				LOGGER.error( "Error getting vm url: " + entry.getValue(), ex );
			}
		}
		return ret;
	}
	
	public static String getConnectorAddress( VirtualMachine vm ) 
		throws IOException, AgentLoadException, AgentInitializationException 
	{
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
		return connectorAddress;
	}
}

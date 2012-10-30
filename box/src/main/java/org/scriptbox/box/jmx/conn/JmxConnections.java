package org.scriptbox.box.jmx.conn;

import java.util.HashMap;
import java.util.Map;

public class JmxConnections {

	private Map<Integer,JmxConnection> byPid = new HashMap<Integer,JmxConnection>();
	private Map<HostPort,JmxConnection> byHostPort = new HashMap<HostPort,JmxConnection>();
	
	private JmxConnectionBuilder builder;

	
	public JmxConnectionBuilder getBuilder() {
		return builder;
	}

	public void setBuilder(JmxConnectionBuilder builder) {
		this.builder = builder;
	}

	synchronized public JmxConnection getConnection( int pid, String command ) throws Exception {
		JmxConnection ret = byPid.get( pid );
		if( ret == null ) {
			if( builder == null ) { 
				throw new RuntimeException( "Could not find virtual machine with pid: " + pid + ", no JMX connection builder defined" );
			}
			ret = builder.getForPid( pid, command );
			if( ret == null ) {
				throw new RuntimeException( "Could not get JMX connection information for pid: " + pid );
			}
			byPid.put( pid, ret );
		}
		return ret;
	}

	synchronized public JmxConnection getConnection( String host, int port ) throws Exception {
		HostPort key = new HostPort(host,port);
		JmxConnection ret = byHostPort.get( key );
		if( ret == null ) {
			ret = new JmxRmiConnection( host, port );
			byHostPort.put( key, ret );
		}
		return ret;
	}
}

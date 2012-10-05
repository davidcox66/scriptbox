package org.scriptbox.box.jmx.proc;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxConnections;

public class JmxRemoteProcessProvider extends JmxProcessProvider {

	private String host;
	private int port;
	
	public JmxRemoteProcessProvider( JmxConnections connections, String name, String host, int port ) {
		super( connections, name );
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run() throws Exception {
		JmxConnection connection = getConnections().getConnection( host, port );
		with( new JmxProcess(getName(),connection) );
	}

}

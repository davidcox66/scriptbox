package org.scriptbox.box.plugins.jmx.proc;

import org.scriptbox.box.plugins.jmx.JmxConnection;
import org.scriptbox.box.plugins.jmx.JmxPlugin;

public class JmxRemoteProcessProvider extends JmxProcessProvider {

	private String host;
	private int port;
	
	public JmxRemoteProcessProvider( JmxPlugin plugin, String name, String host, int port ) {
		super( plugin, name );
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run() throws Exception {
		JmxConnection connection = plugin.getConnections().getConnection( host, port );
		with( new JmxProcess(getName(),connection) );
	}

}

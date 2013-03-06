package org.scriptbox.box.jmx.proc;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.conn.JmxConnections;

public class JmxRemoteProcessProvider extends JmxProcessProvider {

	private String url;
	
	public JmxRemoteProcessProvider( JmxConnections connections, String name, String url ) {
		super( connections, name );
		this.url = url;
	}
	
	@Override
	public void run() throws Exception {
		JmxConnection connection = getConnections().getConnection( url );
		with( new JmxProcess(getName(),connection) );
	}

}

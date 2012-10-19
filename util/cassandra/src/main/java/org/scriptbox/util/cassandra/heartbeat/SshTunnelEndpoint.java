package org.scriptbox.util.cassandra.heartbeat;

import java.io.Serializable;


public class SshTunnelEndpoint extends TcpEndpoint implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String tunnelHost;
	private int tunnelPort;
	
	public SshTunnelEndpoint() {
	}
	
	public SshTunnelEndpoint( String tunnelHost, int tunnelPort, String host, int port ) {
		super( host, port );
		this.tunnelHost = tunnelHost;
		this.tunnelPort = tunnelPort;
	}

	public String getTunnelHost() {
		return tunnelHost;
	}

	public void setTunnelHost(String tunnelHost) {
		this.tunnelHost = tunnelHost;
	}

	public int getTunnelPort() {
		return tunnelPort;
	}

	public void setTunnelPort(int tunnelPort) {
		this.tunnelPort = tunnelPort;
	}

	
}

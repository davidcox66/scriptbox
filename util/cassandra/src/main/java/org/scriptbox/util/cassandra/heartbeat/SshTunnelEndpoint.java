package org.scriptbox.util.cassandra.heartbeat;


public class SshTunnelEndpoint extends TcpEndpoint {

	private String tunnel;
	
	public SshTunnelEndpoint() {
	}
	
	public SshTunnelEndpoint( String tunnel, String host, int port ) {
		super( host, port );
		this.tunnel = tunnel;
	}

	public String getTunnel() {
		return tunnel;
	}

	public void setTunnel(String tunnel) {
		this.tunnel = tunnel;
	}
	
}

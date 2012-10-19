package org.scriptbox.util.cassandra.heartbeat;


public class TcpEndpoint extends Endpoint {

	private String host;
	private int port;
	
	public TcpEndpoint() {
	}
	
	public TcpEndpoint( String host, int port ) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	
}

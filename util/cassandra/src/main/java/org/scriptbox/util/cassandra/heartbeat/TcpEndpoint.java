package org.scriptbox.util.cassandra.heartbeat;

import java.io.Serializable;


public class TcpEndpoint extends Endpoint implements Serializable {

	private static final long serialVersionUID = 1L;
	
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

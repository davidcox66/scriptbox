package org.scriptbox.util.remoting.endpoint;

import java.io.Serializable;


public class SshTunnelEndpoint extends TcpEndpoint implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String tunnelHost;
	private int tunnelPort;
	private String user;
	private String password;
	
	public SshTunnelEndpoint() {
	}
	
	public SshTunnelEndpoint( String tunnelHost, int tunnelPort, String host, int port, String user, String password ) {
		super( host, port );
		this.tunnelHost = tunnelHost;
		this.tunnelPort = tunnelPort;
		this.user = user;
		this.password = password;
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
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((tunnelHost == null) ? 0 : tunnelHost.hashCode());
		result = prime * result + tunnelPort;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SshTunnelEndpoint other = (SshTunnelEndpoint) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (tunnelHost == null) {
			if (other.tunnelHost != null)
				return false;
		} else if (!tunnelHost.equals(other.tunnelHost))
			return false;
		if (tunnelPort != other.tunnelPort)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}

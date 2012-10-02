package org.scriptbox.box.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class BoxTunnel 
{
	private static final Logger LOGGER = LoggerFactory.getLogger( BoxTunnel.class );
	
	private BoxTunnelCredentials credentials;
    private int localPort;
    private String tunnelHost;
    private int tunnelPort = 22;
    private String remoteHost;
    private int remotePort;

    private Session session;
    
    public BoxTunnel() {
    }

    
	public BoxTunnelCredentials getCredentials() {
		return credentials;
	}


	public void setCredentials(BoxTunnelCredentials credentials) {
		this.credentials = credentials;
	}


	public int getLocalPort() {
		return localPort;
	}


	public void setLocalPort(int localPort) {
		this.localPort = localPort;
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


	public String getRemoteHost() {
		return remoteHost;
	}


	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}


	public int getRemotePort() {
		return remotePort;
	}


	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}


	public Session getSession() {
		return session;
	}


	public void setSession(Session session) {
		this.session = session;
	}


	public int connect() throws JSchException {

      JSch jsch=new JSch();
      
      session= jsch.getSession(credentials.getUser(), tunnelHost, tunnelPort);
      session.setUserInfo(credentials);
      session.connect();

      int assignedPort = session.setPortForwardingL(localPort, remoteHost, remotePort);
      LOGGER.debug("localhost:"+assignedPort+" -> " + tunnelHost + ":" + tunnelPort + " -> " + remoteHost+":"+ remotePort );
      return assignedPort;
  }

  public void disconnect() {
	  if( session != null ) {
		  session.disconnect();
	  }
  }
}

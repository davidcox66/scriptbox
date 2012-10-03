package org.scriptbox.util.remoting.tunnel;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class Tunnel 
{
	private static final Logger LOGGER = LoggerFactory.getLogger( Tunnel.class );

	static {
		JSch.setLogger( new com.jcraft.jsch.Logger() {
			public boolean isEnabled(int level) {
				if( level == DEBUG ) {
					return LOGGER.isDebugEnabled();
				}
				else if( level == INFO ) {
					return LOGGER.isInfoEnabled();
				}
				else if( level == WARN ) {
					return LOGGER.isWarnEnabled();
				}
				else if( level == ERROR || level == FATAL ) {
					return LOGGER.isErrorEnabled();
				}
				return false;
			}

			public void log(int level, String message) {
				if( level == DEBUG ) {
					LOGGER.debug( message );
				}
				else if( level == INFO ) {
					LOGGER.info( message );
				}
				else if( level == WARN ) {
					LOGGER.warn( message );
				}
				else if( level == ERROR || level == FATAL ) {
					LOGGER.error( message );
				}
			}
		} );
	}
	private TunnelCredentials credentials;
    private int localPort;
    private String tunnelHost;
    private int tunnelPort = 22;
    private String remoteHost;
    private int remotePort;
    private boolean strictHostChecking=true;
    
    private Session session;
    
    public Tunnel() {
    }

	public TunnelCredentials getCredentials() {
		return credentials;
	}

	public void setCredentials(TunnelCredentials credentials) {
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

	public int connect() throws Exception {

	  assertArg( credentials != null, "No credentials specified");
	  
	  assertArg( StringUtils.isNotEmpty(credentials.getUser()), "No credentials.user specified");
	  // assertArg( localPort != 0, "No local port specified" );
	  
	  assertArg( StringUtils.isNotEmpty(tunnelHost), "No tunnel host specified" );
	  assertArg( tunnelPort != 0, "No tunnel port specified" );
	  
	  assertArg( StringUtils.isNotEmpty(remoteHost), "No remote host specified" );
	  assertArg( remotePort != 0, "No remote port specified" );
	  
      JSch jsch = new JSch();
      loadIdentities( jsch );
      loadKnownHosts( jsch );
      
      LOGGER.debug( "connect: starting tunnel - " + tunnelHost + ":" + tunnelPort + " -> " + remoteHost+":"+ remotePort );
      session= jsch.getSession(credentials.getUser(), tunnelHost, tunnelPort);
      session.setUserInfo(credentials);
      session.setConfig("StrictHostKeyChecking", strictHostChecking? "yes" : "no");
      session.connect();

      int assignedPort = session.setPortForwardingL(localPort, remoteHost, remotePort);
      LOGGER.debug("connect: tunnel esablisted - localhost:"+assignedPort+" -> " + tunnelHost + ":" + tunnelPort + " -> " + remoteHost+":"+ remotePort );
      return assignedPort;
  }

  private void loadKnownHosts( JSch jsch ) throws Exception {
	 String home = System.getProperty("user.home");
	 File sshDir = new File( home, ".ssh" );
	 if( sshDir.isDirectory() ) {
		 File knownHosts = new File( sshDir, "known_hosts" ); 
		 if( knownHosts.exists() ) {
			 LOGGER.debug( "loadKnownHosts: adding " + knownHosts );
			 jsch.setKnownHosts( knownHosts.getAbsolutePath() );
		 }
	 }
  }
  private void loadIdentities( JSch jsch ) throws Exception {
	  String home = System.getProperty("user.home");
	  File sshDir = new File( home, ".ssh" );
	  if( sshDir.isDirectory() ) {
		  File[] privateKeys = sshDir.listFiles( new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith("id_") && !name.endsWith(".pub")) || name.endsWith(".pem"); 
			}
		});
		for( File privateKey : privateKeys ) {
			LOGGER.debug( "loadIdentities: adding " + privateKey );
			File publicKey = new File( privateKey.getAbsolutePath() +".pub" );
			if( publicKey.exists() ) {
				LOGGER.debug( "loadIdentities: adding " + privateKey + " + " + publicKey );
				jsch.addIdentity(privateKey.getAbsolutePath(), publicKey.getAbsolutePath(), null );
				
			}
			else {
				LOGGER.debug( "loadIdentities: adding " + privateKey );
				jsch.addIdentity(privateKey.getAbsolutePath() );
			}
		}
	  }
  }
  
  private void assertArg( boolean arg, String message ) {
	  if( !arg ) {
		  throw new IllegalArgumentException( message ); 
	  }
  }
  
  public void disconnect() {
	  if( session != null ) {
		  session.disconnect();
	  }
  }
}

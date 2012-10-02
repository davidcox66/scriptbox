package org.scriptbox.box.remoting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.UserInfo;

public class BoxTunnelCredentials implements UserInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxTunnelCredentials.class );
	
	private String user;
	private String passphrase;
	private String password;

	private boolean askedForPassword;
	private boolean askedForPassphrase;
	
	public BoxTunnelCredentials() {
	}
	
	public BoxTunnelCredentials( String user, String passphrase, String password ) {
		this.user = user;
		this.passphrase = passphrase;
		this.password = password;
	}
	
	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getUser() {
		LOGGER.debug( "getUser: retrieving user");
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public String getPassphrase() {
		LOGGER.debug( "getPassphrase: retrieving passphrase");
		return passphrase;
	}

	@Override
	public String getPassword() {
		LOGGER.debug( "getPassword: retrieving password");
		return password;
	}

	@Override
	public boolean promptPassword(String message) {
		LOGGER.debug( "promptPassword: retrieving password: " + message );
		if( askedForPassword ) {
			throw new RuntimeException( "Continuing to ask for password, so aborting.");
		}
		askedForPassword = true;
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		LOGGER.debug( "promptPassphrase: retrieving passphrase: " + message );
		if( askedForPassphrase ) {
			throw new RuntimeException( "Continuing to ask for passphrase, so aborting.");
		}
		askedForPassphrase = true;
		return true;
	}

	@Override
	public boolean promptYesNo(String message) {
		LOGGER.debug( "promptYesNo: asking yes/no: " + message );
		return true;
	}

	@Override
	public void showMessage(String message) {
		LOGGER.debug( "showMessage: " + message );
	}

}

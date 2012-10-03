package org.scriptbox.util.remoting.tunnel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.UserInfo;

public class TunnelCredentials implements UserInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger( TunnelCredentials.class );

	public static final int RETRIES = 0;
	
	private String user;
	private String passphrase;
	private String password;

	private int maxPasswordRetries = RETRIES;
	private int maxPassphraseRetries = RETRIES;

	private int passwordRetries;
	private int passphraseRetries;
	
	public TunnelCredentials() {
	}
	
	public TunnelCredentials( String user, String passphrase, String password ) {
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
		LOGGER.debug( "getUser: retrieving user: '" + user + "'");
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
		LOGGER.debug( "getPassword: retrieving password: '" + password + "'");
		return password;
	}

	
	public int getMaxPasswordRetries() {
		return maxPasswordRetries;
	}

	public void setMaxPasswordRetries(int maxPasswordRetries) {
		this.maxPasswordRetries = maxPasswordRetries;
	}

	public int getMaxPassphraseRetries() {
		return maxPassphraseRetries;
	}

	public void setMaxPassphraseRetries(int maxPassphraseRetries) {
		this.maxPassphraseRetries = maxPassphraseRetries;
	}

	@Override
	public boolean promptPassword(String message) {
		LOGGER.debug( "promptPassword: retrieving password: " + message );
		if( maxPasswordRetries > 0 && passwordRetries++ >= maxPasswordRetries ) {
			throw new RuntimeException( "Continuing to ask for password, so aborting.");
		}
		return true;
	}

	@Override
	public boolean promptPassphrase(String message) {
		LOGGER.debug( "promptPassphrase: retrieving passphrase: " + message );
		if( maxPassphraseRetries > 0 && passphraseRetries++ >= maxPassphraseRetries ) {
			throw new RuntimeException( "Continuing to ask for passphrase, so aborting.");
		}
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

package org.scriptbox.ui.shared;

import java.io.Serializable;

public class ClientData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String clientId;
	private String keyId;
	private String salt;
	private String secretCredentials;
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getKeyId() {
		return keyId;
	}
	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getSecretCredentials() {
		return secretCredentials;
	}
	public void setSecretCredentials(String secretCredentials) {
		this.secretCredentials = secretCredentials;
	}
	
	
}

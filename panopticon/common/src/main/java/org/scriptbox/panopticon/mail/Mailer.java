package org.scriptbox.panopticon.mail;

import javax.mail.Session;
import java.util.Properties;

public class Mailer {

	public void setHost( String host ) {
		 Properties props = System.getProperties();
	     props.put("mail.smtp.host", host );
	     Session session = Session.getDefaultInstance(props, null);
	}
	
	public String getHost() {
		 Properties props = System.getProperties();
	     return props.getProperty("mail.smtp.host");
	}
	
	public MailMessage message() {
		return new MailMessage();
	}
}

package org.scriptbox.panopticon.mail;

public class GroovyMailer extends Mailer {

	public GroovyMailMessage message() {
		return new GroovyMailMessage();
	}
}

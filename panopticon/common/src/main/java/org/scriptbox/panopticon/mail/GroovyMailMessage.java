package org.scriptbox.panopticon.mail;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

import java.lang.ClassNotFoundException;
import java.io.IOException;
import java.util.Map;

public class GroovyMailMessage extends MailMessage {

	public void addTemplateText( String text, Map parameters ) throws IOException, ClassNotFoundException {

		SimpleTemplateEngine engine = new SimpleTemplateEngine();
		Template template = engine.createTemplate(text);
		addText( template.make(parameters).toString() );
	}
}

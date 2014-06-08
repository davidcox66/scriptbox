package org.scriptbox.panopticon.mail;

import groovy.lang.Closure;

import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;

public class MailGroovyInjector implements MailInjector {

	private MailPlugin plugin;

	
	public MailPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(MailPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "mailer", new GroovyMailer()  );
	}
}

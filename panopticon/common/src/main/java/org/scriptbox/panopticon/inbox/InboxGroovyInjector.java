package org.scriptbox.panopticon.inbox;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboxGroovyInjector implements InboxInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( InboxGroovyInjector.class );
	
	private InboxPlugin plugin;

	
	public InboxPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(InboxPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "inbox", new MethodClosure( this, "inbox")  );
	}
	
	public Inbox inbox() {
		return new InboxGroovy( BoxContext.getCurrentContext() );
	}
}

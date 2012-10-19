package org.scriptbox.panopticon.top;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;

public class TopGroovyInjector implements TopInjector {

	private TopPlugin plugin;

	
	public TopPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(TopPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "top", new MethodClosure( this, "top")  );
	}
	
    public void top( int delay ) {
    	plugin.top( delay );
    }
}

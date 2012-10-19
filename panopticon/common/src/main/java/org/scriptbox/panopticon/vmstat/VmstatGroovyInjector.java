package org.scriptbox.panopticon.vmstat;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;

public class VmstatGroovyInjector implements Vmstatnjector {

	private VmstatPlugin plugin;

	
	public VmstatPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(VmstatPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "vmstat", new MethodClosure( this, "vmstat")  );
	}
	
    public void vmstat( int delay ) {
    	plugin.vmstat( delay );
    }
}

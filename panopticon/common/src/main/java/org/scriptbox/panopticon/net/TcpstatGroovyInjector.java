package org.scriptbox.panopticon.net;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;

public class TcpstatGroovyInjector implements TcpstatInjector {

	private TcpstatPlugin plugin;

	
	public TcpstatPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(TcpstatPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "tcpstat", new MethodClosure( this, "tcpstat")  );
	}
	
	public void tcpstat( int delay, String interfaceName ) {
		tcpstat( delay, interfaceName, null, null ) ;
	}
   
    public void tcpstat( int delay, String interfaceName, String expression, String tag ) {
    	plugin.tcpstat(delay, interfaceName, expression, tag);
    }
}

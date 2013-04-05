package org.scriptbox.panopticon.net;

import groovy.lang.Closure;

import java.util.Map;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.util.common.obj.ParameterizedRunnable;

public class TcpdumpGroovyInjector implements TcpdumpInjector {

	private TcpdumpPlugin plugin;

	
	public TcpdumpPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(TcpdumpPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "tcpdump", new MethodClosure( this, "tcpdump")  );
	}
	
    public void tcpdump( boolean sudo, int delay, String interfaceName, String expression, String tag, final Closure processor, final Closure summary ) {
    	ParameterizedRunnable<TcpdumpRecord> processorR = 
    		new ParameterizedRunnable<TcpdumpRecord>() {
    			public void run( TcpdumpRecord rec ) throws Exception {
    				processor.call( rec );
    			}
    	};
    	ParameterizedRunnable<Map<String,Number>> summaryR = 
    		new ParameterizedRunnable<Map<String,Number>>() {
    			public void run( Map<String,Number> map ) throws Exception {
    				summary.call( map );
    			}
    	};
    	plugin.tcpdump(sudo, delay, interfaceName, expression, tag, processorR, summaryR );
    }
}

package org.scriptbox.panopticon.apache;

import groovy.lang.Closure;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;

public class ApacheGroovyInjector implements ApacheInjector {

	private ApachePlugin plugin;

	
	public ApachePlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(ApachePlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "apacheResetMetrics", new MethodClosure( this, "apacheResetMetrics")  );
		vars.put( "apacheStatus", new MethodClosure( this, "apacheStatus")  );
		vars.put( "apacheLog", new MethodClosure( this, "apacheLog")  );
		vars.put( "apacheBalancer", new MethodClosure( this, "apacheBalancer")  );
	}
 
	public void apacheStatus( String url ) {
		plugin.status( url );
	}

	public void apacheLog( String location, final Closure closure ) {
		plugin.log( location,  new ParameterizedRunnableWithResult<String,String>() {
			public String run( String param ) {
				return (String)closure.call( param );
			}
		} );
	}
  
	public void apacheBalancer( String location ) {
		plugin.balancer( location );
	}

	public void apacheResetMetrics( String processPattern ) {
		plugin.reset( processPattern );
	}
}

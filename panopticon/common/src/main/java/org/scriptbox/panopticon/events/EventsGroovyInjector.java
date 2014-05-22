package org.scriptbox.panopticon.events;

import groovy.lang.Closure;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsGroovyInjector implements EventslInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( EventsGroovyInjector.class );
	
	private EventsPlugin plugin;

	
	public EventsPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(EventsPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "bus", context.getBeans().get("EventBus", EventBus.class) );
		vars.put( "raise", new MethodClosure( this, "raise")  );
		vars.put( "observe", new MethodClosure( this, "observe")  );
	}
	
	public void raise( Object... args ) {
		plugin.raise( args );
	}
	
	public void observe( final Object event, final Closure closure ) {
		plugin.subscribe( event, new ParameterizedRunnable<Object>() {
			public void run( Object arg ) throws Exception {
				closure.call( (Object[])arg );
			}
			
		} );
	}
}

package org.scriptbox.panopticon.gauntlet;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GauntletGroovyInjector implements GauntletInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( GauntletGroovyInjector.class );
	
	private GauntletPlugin plugin;

	
	public GauntletPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(GauntletPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "gauntlet", new MethodClosure( this, "gauntlet")  );
	}
	
	public Gauntlet gauntlet() {
		return gauntlet( false );
	}
	
	public Gauntlet gauntlet( boolean immediate ) {
		return new GauntletGroovy( BoxContext.getCurrentContext(), immediate );
	}
}

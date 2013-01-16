package org.scriptbox.horde.pool;

import java.util.Map;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolGroovyInjector implements PoolInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( PoolGroovyInjector.class );
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "pool", new MethodClosure( this, "pool")  );
	}
	
	public GroovyPool pool( Map<String,Object> params ) {
		return new GroovyPool( params );
	}
}

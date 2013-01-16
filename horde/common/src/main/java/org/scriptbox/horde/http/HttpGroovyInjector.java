package org.scriptbox.horde.http;

import org.codehaus.groovy.runtime.MethodClosure;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpGroovyInjector implements HttpInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( HttpGroovyInjector.class );
	
	@Override
	public String getLanguage() {
		return "groovy";
	}

	@Override
	public void inject(BoxContext context) {
		Lookup vars = context.getScriptVariables();
		vars.put( "http", new MethodClosure( this, "http")  );
	}
	
	public GroovyHttp http() {
		return new GroovyHttp();
	}
}

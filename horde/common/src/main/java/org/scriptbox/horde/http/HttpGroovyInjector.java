package org.scriptbox.horde.http;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
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
		vars.put( "httpClient", new MethodClosure( this, "httpClient")  );
		vars.put( "cookieStore", new MethodClosure( this, "cookieStore")  );
	}
	
	public GroovyHttp httpClient() {
		return new GroovyHttp();
	}
	
	public CookieStore cookieStore() {
		return new BasicCookieStore();
	}
}

package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GroovySeleniumShell extends SeleniumMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumShell.class );

    private Binding binding;

    public GroovySeleniumShell(SeleniumService service)  {
		super( service );
        bind();
    }

	public void run( String script, List<String> parameters ) {
		GroovyShell engine = new GroovyShell( binding );
		engine.run(script, "SeleniumScript", parameters);
	}

    private void bind() {
		binding = new Binding();

		Logger logger = LoggerFactory.getLogger( "SeleniumScript" );
    	binding.setVariable("log", logger);
    	binding.setVariable("logger", logger);
    	binding.setVariable("LOGGER", logger);

		new GroovySeleniumBinder( binding, this ).bind();
    }
}

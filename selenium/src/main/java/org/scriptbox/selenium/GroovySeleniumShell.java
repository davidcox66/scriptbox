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

    	binding.setVariable("log", LOGGER);
    	binding.setVariable("logger", LOGGER);
    	binding.setVariable("LOGGER", LOGGER);

		new GroovySeleniumBinder( binding, this ).bind();
    }
}

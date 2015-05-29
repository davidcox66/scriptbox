package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GroovySeleniumShell {

    private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumShell.class );

	private static final AtomicInteger counter = new AtomicInteger();

    private Binding binding;
	private GroovyShell engine;

    public GroovySeleniumShell(Binding binding)  {
		this.binding = binding;

		Logger logger = LoggerFactory.getLogger( "SeleniumScript" );
		binding.setVariable("log", logger);
		binding.setVariable("logger", logger);
		binding.setVariable("LOGGER", logger);

		engine = new GroovyShell( binding );
    }

	public GroovyShell getEngine() {
		return engine;
	}

	public void run( File file, List<String> parameters ) throws IOException {
		engine.run(file, parameters);
	}

	public void run( String script, List<String> parameters ) {
		engine.run(script, "SeleniumScript" + counter.getAndIncrement(), parameters);
	}
}

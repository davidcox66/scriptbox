package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.scriptbox.selenium.bind.BindUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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

		BindUtils.bind(binding, this, "define");
		BindUtils.bind(binding, this, "getVariable");
		BindUtils.bind(binding, this, "setVariable");
		BindUtils.bind(binding, this, "getVariables");

		BindUtils.bind(binding, this, "exit");
		BindUtils.bind(binding, this, "usage");

        binding.setVariable( "binding", binding );

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

	public void define( String name, Object value ) {
		LOGGER.trace( "define: name=" + name + ", value=" + value );
		binding.setVariable(name, value);
	}

	public Object getVariable( String name ) {
		return binding.getVariable( name );
	}

	public void setVariable( String name, Object value ) {
		binding.setVariable( name, value );
	}

	public void usage( int count, String msg ) {
		String[] args = (String[])binding.getVariable( "args" );
		if( args == null || args.length < count ) {
            usage( msg );
		}
	}

	public void usage( String msg ) {
		System.err.println( "Usage: " + binding.getVariable("scriptName") + " " + msg );
		System.exit( 1 );
	}

	public void exit() {
		exit( 0 );
	}

	public void exit(  String msg ) {
		exit( 1, msg );
	}

	public void exit( int code ) {
		LOGGER.debug( "exit: script requested exit with code: " + code );
		System.exit( code );
	}

	public void exit( int code, String msg ) {
		LOGGER.debug( "exit: script requested exit with code: " + code + ", message: " + msg );
		System.err.println( msg );
		System.exit( code );
	}

	public Map getVariables() {
		return binding.getVariables();
	}
}

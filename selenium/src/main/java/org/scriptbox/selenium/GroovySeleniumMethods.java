package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GroovySeleniumMethods extends SeleniumMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumMethods.class );

    private Binding binding;

    public GroovySeleniumMethods( SeleniumService service )  {
		super( service );
        bind();
    }

	public void run( String includeText, String scriptText, List<String> parameters ) {
		String text = StringUtils.isNotEmpty(includeText) ? includeText + "\n" + scriptText : scriptText;
		GroovyShell engine = new GroovyShell( binding );
		engine.run(text, "SeleniumScript", parameters);
	}

    private void bind() {
		binding = new Binding();

    	binding.setVariable("log", LOGGER);
    	binding.setVariable("logger", LOGGER);
    	binding.setVariable("LOGGER", LOGGER);

		new GroovySeleniumBinder( binding, this ).bind();
    }
}

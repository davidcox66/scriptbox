package org.scriptbox.selenium.ext.misc;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 5/28/15.
 */
public class UtilsExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilsExtension.class);

    public void configure() throws Exception {
        bind(context.getBinding());
    }

    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "batcher");
    }

    public Batcher batcher( int size, Closure closure ) {
        return new Batcher( size, closure );
    }

}

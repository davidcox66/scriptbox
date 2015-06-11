package org.scriptbox.selenium.ext;

import groovy.lang.Binding;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 5/28/15.
 */
public class UtilsExtension implements Bindable, SeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(UtilsExtension.class);

    public void init( SeleniumExtensionContext ctx ) {
        bind( ctx.getBinding() );
    }

    @Override
    public void bind(Binding binding) {
    }
}

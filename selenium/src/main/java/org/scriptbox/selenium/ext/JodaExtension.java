package org.scriptbox.selenium.ext;

import groovy.lang.Binding;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * Created by david on 5/28/15.
 */
public class JodaExtension implements Bindable, SeleniumExtension {

    public void init( SeleniumExtensionContext ctx ) {
        bind( ctx.getBinding() );
    }

    @Override
    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "dateTimeFormat");
    }

    public DateTimeFormatter dateTimeFormat( String format ) {
        return DateTimeFormat.forPattern(format);
    }

}

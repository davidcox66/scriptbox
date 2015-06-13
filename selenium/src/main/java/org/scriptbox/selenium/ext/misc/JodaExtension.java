package org.scriptbox.selenium.ext.misc;

import groovy.lang.Binding;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.base.BaseLocal;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;

/**
 * Created by david on 5/28/15.
 */
public class JodaExtension extends AbstractSeleniumExtension {

    public void configure() throws Exception {
        bind(context.getBinding());
    }

    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "dateTimeFormat");
        BindUtils.bind(binding, this, "parseLocalDate");
        BindUtils.bind(binding, this, "parseLocalDateTime");
        BindUtils.bind(binding, this, "formatLocal");
    }

    public DateTimeFormatter dateTimeFormat( String format ) {
        return DateTimeFormat.forPattern(format);
    }

    public LocalDate parseLocalDate( String val, String format ) {
        return DateTimeFormat.forPattern(format).parseLocalDate( val ) ;
    }

    public LocalDateTime parseLocalDateTim( String val, String format ) {
        return DateTimeFormat.forPattern(format).parseLocalDateTime(val) ;
    }

    public String formatLocal( BaseLocal val, String format ) {
        StringBuffer ret = new StringBuffer();
        DateTimeFormat.forPattern(format).printTo( ret, val ) ;
        return ret.toString();
    }
}

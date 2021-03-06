package org.scriptbox.selenium.ext.offline;

import groovy.lang.Binding;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * Created by david on 5/28/15.
 */
public class CsvExtension extends AbstractSeleniumExtension {

    public void configure() throws Exception {
        bind(context.getBinding());
    }

    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "csv");
    }

    public CSVParser csv( String text, String format ) throws IOException {
        return CSVParser.parse( text, getFormat(format) );
    }

    public CSVParser csv( File file, String format ) throws IOException {
        return CSVParser.parse(file, Charset.defaultCharset(), getFormat(format));
    }

    private CSVFormat getFormat( String format ) {
        try {
            Field field = CSVFormat.class.getField(format);
            return (CSVFormat)field.get( null );

        }
        catch( Exception ex ) {
            throw new RuntimeException( "Invalid CSV format: '" + format + "'");
        }
    }
}

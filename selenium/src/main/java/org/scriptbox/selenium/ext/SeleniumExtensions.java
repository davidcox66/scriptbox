package org.scriptbox.selenium.ext;

import groovy.lang.GroovyClassLoader;
import org.scriptbox.selenium.CommandLineUtil;
import org.scriptbox.selenium.ext.contact.ContactExtension;
import org.scriptbox.selenium.ext.event.EventBusExtension;
import org.scriptbox.selenium.ext.offline.CsvExtension;
import org.scriptbox.selenium.ext.offline.DownloadsExtension;
import org.scriptbox.selenium.ext.offline.HttpConnectorExtension;
import org.scriptbox.selenium.ext.misc.UtilsExtension;
import org.scriptbox.selenium.ext.misc.JodaExtension;
import org.scriptbox.selenium.ext.misc.MongoExtension;
import org.scriptbox.selenium.ext.misc.QuartzExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 6/12/15.
 */
public class SeleniumExtensions {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumExtensions.class);

    private SeleniumExtensionContext context;
    private List<SeleniumExtension> extensions = new ArrayList<SeleniumExtension>();

    public SeleniumExtensions( SeleniumExtensionContext context ) {
        this.context = context;
        build();
    }

    private void build() {

        context.setExtensions(extensions);

        extensions.add(new ShellExtension());
        extensions.add(new UtilsExtension());
        extensions.add(new CsvExtension());
        extensions.add(new JodaExtension());
        extensions.add(new DownloadsExtension());
        extensions.add(new HttpConnectorExtension());
        extensions.add(new MongoExtension());
        extensions.add(new QuartzExtension());
        extensions.add(new EventBusExtension());
        extensions.add(new ContactExtension());
        extensions.add(new UserDefinedExtension());
        extensions.add(new LibsExtension());
    }

    public void init() {
        for( SeleniumExtension ext : extensions )  {
            ext.init(context);
        }
    }

    public void configure() throws Exception {
        for( SeleniumExtension ext : extensions )  {
            ext.configure();
        }
    }

    public void run() throws Exception {
        for( SeleniumExtension ext : extensions )  {
            ext.run();
        }
    }


}

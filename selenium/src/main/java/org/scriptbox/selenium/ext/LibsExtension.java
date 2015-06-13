package org.scriptbox.selenium.ext;

import org.scriptbox.selenium.CommandLineUtil;
import org.scriptbox.selenium.GroovySeleniumShell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by david on 5/29/15.
 */
public class LibsExtension implements SeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger( LibsExtension.class );

    private SeleniumExtensionContext context;

    public void init(SeleniumExtensionContext context) {
        this.context = context;
        context.addUsage( "[--libs=<filespec>]");
    }

    public void configure() throws Exception {

        String libs = context.getCommandLine().consumeArgValue("libs", false);
        if( libs != null ) {
            GroovySeleniumShell shell = context.getShell();
            List<String> parameters = context.getCommandLine().getParameters();
            List<File> files = CommandLineUtil.resolveFiles(libs, ".groovy");
            for (File file : files) {
                LOGGER.trace( "init: running lib=" + file );
                try {
                    shell.run(file, parameters);
                }
                catch( Exception ex ) {
                    throw new RuntimeException( "Failed initializing library: " + file.getAbsolutePath(), ex );
                }
            }
        }
    }

    public void run() { }

}

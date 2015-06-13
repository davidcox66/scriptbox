package org.scriptbox.selenium.ext;

import groovy.lang.GroovyClassLoader;
import org.scriptbox.selenium.CommandLineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by david on 6/12/15.
 */
public class UserDefinedExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDefinedExtension.class);

    public void init( SeleniumExtensionContext context ) {
        super.init( context );
        context.addUsage( "[--exts=<filespec>]");
    }
    public void configure() throws Exception {
        List<SeleniumExtension> extensions = context.getExtensions();
        String spec = context.getCommandLine().consumeArgValue( "exts", false );
        if( spec != null ) {
            List<File> files = CommandLineUtil.resolveFiles(spec, ".groovy");
            if( !files.isEmpty() ) {
                GroovyClassLoader gcl = context.getShell().getEngine().getClassLoader();
                for( File file : files ) {
                    Class cls = gcl.parseClass( file );
                    if( !SeleniumExtension.class.isAssignableFrom(cls) ) {
                        throw new RuntimeException( "File does not contain an extension: '" + file.getAbsolutePath() + "'");
                    }
                    SeleniumExtension instance = (SeleniumExtension)cls.newInstance();
                    LOGGER.debug("configure: adding extension - class: " + cls + ", instance=" + instance);
                    extensions.add(instance);
                }
            }
        }
    }

}

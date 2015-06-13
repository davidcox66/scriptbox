package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import org.scriptbox.selenium.driver.DriverType;
import org.scriptbox.selenium.driver.SeleniumController;
import org.scriptbox.selenium.ext.*;
import org.scriptbox.selenium.ext.offline.CsvExtension;
import org.scriptbox.selenium.ext.offline.DownloadsExtension;
import org.scriptbox.selenium.ext.offline.HttpConnectorExtension;
import org.scriptbox.selenium.ext.event.EventBusExtension;
import org.scriptbox.selenium.ext.contact.ContactExtension;
import org.scriptbox.selenium.remoting.ClientSeleniumService;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GroovySeleniumCli {

	private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumCli.class );

    private static CommandLine cmd;
    private static SeleniumExtensions extensions;
    private static SeleniumExtensionContext context;

    public static void main( String[] args ) {

    	try {
    		cmd = new CommandLine( args );
    		
    		// just stripping this off if used in wrapper script for debug logging
    		cmd.consumeArg("debug"); 
    		cmd.consumeArg("trace");

            context = new SeleniumExtensionContext();
            context.setCommandLine( cmd );
            context.setBinding( new Binding() );

            extensions = new SeleniumExtensions( context );
            extensions.init();

    		boolean cli = cmd.hasArgValue("client");

    		if( cli ) {
				client();
    		}
			else {
				server();
    		}
        }
    	catch( CommandLineException ex ) {
            ex.printStackTrace( System.err );
            usage();
    	}
        catch(Exception ex ) {
            ex.printStackTrace( System.err );
        }
    }

    private static void client() throws Exception {
        extensions.configure();
        extensions.run();
    }

    private static void server() throws Exception {
        int port = cmd.consumeArgValueAsInt("server",false);
        Timeout timeout = new Timeout();
        timeout.setWait( cmd.consumeArgValueAsInt( "timeout", false) );
        timeout.setWait( cmd.consumeArgValueAsInt( "script-timeout", false) );
        timeout.setWait(cmd.consumeArgValueAsInt("load-timeout", false));

		SeleniumController selenium = new SeleniumController( getDriverType(cmd) );
		selenium.setTimeout( timeout );
		selenium.setExe(cmd.consumeArgValue("exe", false));
		selenium.setProfile( cmd.consumeArgValue("profile", false) );
        selenium.getOptions().setDownloadDirectory( getDownloadDirectory() );
        selenium.getOptions().setExtensions( getBrowserExtensions() );

		if( selenium.getExe() != null ) {
			selenium.setPort( port + 1 );
			setUrl(selenium, "http://localhost:" + (port + 1));
		}
        else {
            String ru = cmd.consumeArgValue( "url", true);
            setUrl(selenium, ru);
        }

		SeleniumController.setInstance( selenium );

        JettyService jetty = new JettyService("classpath:selenium-context.xml");
        jetty.setHttpPort( port );
        jetty.start();
        while( true ) {
            System.out.print(".");
            Thread.sleep( 60*1000 );
        }
	}

    private static List<File> getBrowserExtensions() throws CommandLineException {
        String extdir = cmd.consumeArgValue( "browser-ext-dir", false );
        if( extdir != null ) {
            List<File> files = new ArrayList<File>();
            String extarg = cmd.consumeArgValue("browser-exts", true );
            String[] exts = extarg.split(",");
            for( String ext : exts ) {
                files.add( new File(extdir,ext) );
            }
            return files;
        }
        return null;

    }
    private static File getDownloadDirectory() throws CommandLineException
    {
        String dir = cmd.consumeArgValue("download-dir",false);
        if( dir != null ) {
            File fd = new File( dir );
            if( !fd.exists() ) {
                if( !fd.mkdirs() ) {
                    throw new RuntimeException( "Download directory '" + dir + "' does not exist and could not be created");
                }
                else if( !fd.canWrite() ) {
                    throw new RuntimeException( "Download directory '" + dir + "' is not writable");
                }
            }
            return fd;
        }
        return null;

    }

	private static void setUrl( SeleniumController controller, String url ) {
		try {
			controller.setUrl(new URL(url));
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Invalid remote url: '" + url + "'", ex );
		}
	}

    private static void usage() {
        System.err.println( "Usage: GroovySeleniumCli " +
        	"{--firefox [--profile <profile path>] | --chrome [--url <url>] | --ie} " +
            "{--client=<server url> " + context.getUsages() + " | " +
             "--server=<port> [--download-dir=<dir>] [--timeout={<seconds>|30}] [--script-timeout=<seconds>] [--load-timeout=<seconds>] " +
            "[--browser-ext-dir=<directory> --browser-exts=<ext...>] } <arg>..." );
        System.exit( 1 );
    }

    private static DriverType getDriverType( CommandLine cmd ) throws CommandLineException {
    	DriverType[] types = DriverType.values();
    	for( DriverType type : types ) {
    		if( cmd.consumeArg(type.getName()) ) {
    			return type;
    		}
    	}
    	usage();
    	return null;
    }
}

package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import org.scriptbox.selenium.driver.DriverType;
import org.scriptbox.selenium.driver.SeleniumController;
import org.scriptbox.selenium.ext.*;
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
	
    public static void main( String[] args ) {

    	try {
    		CommandLine cmd = new CommandLine( args );
    		
    		// just stripping this off if used in wrapper script for debug logging
    		cmd.consumeArg("debug"); 
    		cmd.consumeArg("trace"); 
    		
    		int serverPort = cmd.consumeArgValueAsInt("server",false);
    		String serverUrl = cmd.consumeArgValue("client",false);

    		if( serverPort == 0 ) {
				client( serverUrl, cmd );
    		}
			else {
				server( serverPort, cmd );
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

	private static void server( int port, CommandLine cmd ) throws Exception {
        Timeout timeout = new Timeout();
        timeout.setWait( cmd.consumeArgValueAsInt( "timeout", false) );
        timeout.setWait( cmd.consumeArgValueAsInt( "script-timeout", false) );
        timeout.setWait(cmd.consumeArgValueAsInt("load-timeout", false));

		SeleniumController selenium = new SeleniumController( getDriverType(cmd) );
		selenium.setTimeout( timeout );
		selenium.setExe(cmd.consumeArgValue("exe", false));
		selenium.setProfile( cmd.consumeArgValue("profile", false) );
        selenium.getOptions().setDownloadDirectory( getDownloadDirectory(cmd) );
        selenium.getOptions().setExtensions( getBrowserExtensions(cmd) );

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

    private static List<File> getBrowserExtensions( CommandLine cmd ) throws CommandLineException {
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
    private static File getDownloadDirectory( CommandLine cmd ) throws CommandLineException
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

	private static void client( String serverHostPort, CommandLine cmd ) throws Exception {

        Binding binding = new Binding();
		ClientSeleniumService service = new ClientSeleniumService( serverHostPort );
        GroovySeleniumMethods methods = new GroovySeleniumMethods( service );
        GroovySeleniumShell shell = new GroovySeleniumShell( binding );

        int wait = cmd.consumeArgValueAsInt( "timeout", false );
        if( wait > 0 ) {
            methods.setWait( wait );
        }

        SeleniumExtensionContext ctx = new SeleniumExtensionContext();
        ctx.setService( service );
        ctx.setCommandLine( cmd );
        ctx.setBinding( binding );
        ctx.setShell(shell);
        ctx.setMethods(methods);

        initExtensions(ctx);

        List<String> parameters = cmd.getParameters();
        File file = getScriptFile( cmd );
        cmd.checkUnusedArgs();

        setScriptName( binding, file );

        LOGGER.trace("client: running script=" + file);
        shell.run(file, parameters);
	}

    private static File getScriptFile( CommandLine cmd ) throws CommandLineException {
        String script = cmd.consumeArgValue("script", true);
        if (!script.endsWith(".groovy")) {
            script += ".groovy";
        }
        return new File(script);
    }

    private static void setScriptName( Binding binding, File file ) {
        String scriptName = file.getName();
        int pos = scriptName.lastIndexOf(".");
        if( pos != -1 ) {
            scriptName = scriptName.substring(0,pos);
        }
        binding.setVariable( "scriptName", scriptName );
    }

    private static void initExtensions( SeleniumExtensionContext ctx ) throws Exception {

        List<SeleniumExtension> exts = new ArrayList<SeleniumExtension>();
        exts.add(new CsvExtension() );
        exts.add(new JodaExtension() );
        exts.add(new DownloadsExtension());
        exts.add(new HttpConnectorExtension());
        exts.add(new MongoExtension());
        exts.add(new QuartzExtension());
        exts.add(ctx.getMethods());

        addUserDefinedExtensions( ctx, exts );

        exts.add(new LibsExtension());

        ctx.setExtensions( exts );

        for( SeleniumExtension ext : exts )  {
            ext.init( ctx );
        }
    }

    private static void addUserDefinedExtensions( SeleniumExtensionContext ctx, List<SeleniumExtension> exts ) throws Exception {
        String spec = ctx.getCommandLine().consumeArgValue( "exts", false );
        if( spec != null ) {
            List<File> files = CommandLineUtil.resolveFiles( spec, ".groovy" );
            if( !files.isEmpty() ) {
                GroovyClassLoader gcl = ctx.getShell().getEngine().getClassLoader();
                for( File file : files ) {
                    Class cls = gcl.parseClass( file );
                    if( !SeleniumExtension.class.isAssignableFrom(cls) ) {
                        throw new RuntimeException( "File does not contain an extension: '" + file.getAbsolutePath() + "'");
                    }
                    SeleniumExtension instance = (SeleniumExtension)cls.newInstance();
                    LOGGER.debug("initExtensions: adding extension - class: " + cls + ", instance=" + instance);
                    exts.add(instance);
                }
            }
        }
    }


    private static void usage() {
        System.err.println( "Usage: GroovySeleniumCli " +
        	"{--firefox [--profile <profile path>] | --chrome [--url <url>] | --ie} " +
        	"--script=<script file>  " +
            "{--client=<server url> [--mongo=[<address>]] [--libs=<files>] [--exts=<files>] | " +
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

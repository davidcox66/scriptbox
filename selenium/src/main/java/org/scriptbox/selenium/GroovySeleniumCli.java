package org.scriptbox.selenium;

import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

import java.io.*;
import java.net.URL;
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
            usage();
            ex.printStackTrace( System.err );
    	}
        catch(Exception ex ) {
            ex.printStackTrace( System.err );
        }
    }

	private static void server( int port, CommandLine cmd ) throws Exception {
		SeleniumController selenium = new SeleniumController( getDriverType(cmd) );
		selenium.setTimeout( cmd.consumeArgValueAsInt( "timeout", false) );
		selenium.setExe( cmd.consumeArgValue("exe",false) );
		selenium.setProfile( cmd.consumeArgValue("profile", false) );

		String ru = cmd.consumeArgValue( "url", true);
		try {
			selenium.setUrl( new URL(ru) );
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Invalid remote url: '" + ru + "'", ex );
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

	private static void client( String serverHostPort, CommandLine cmd ) throws Exception {
		String include = cmd.consumeArgValue("include", false);
		String includeText = include != null ? getText(new File(include)) : null;

		String script = cmd.consumeArgValue("script", true);
		String scriptText = getText( new File(script) );
		List<String> parameters = cmd.getParameters();

		cmd.checkUnusedArgs();

        HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
        factory.setServiceUrl("http://" + serverHostPort + "/remoting/selenium/");
        factory.setServiceInterface(SeleniumService.class);
        factory.afterPropertiesSet();

        SeleniumService service = (SeleniumService)factory.getObject();
		ClientSeleniumService client = new ClientSeleniumService( service );
        GroovySeleniumMethods methods = new GroovySeleniumMethods( client );

        methods.run( includeText, scriptText, parameters );
	}

    private static void usage() {
        System.err.println( "Usage: GroovySeleniumCli " +
        	"{--firefox [--profile <profile path>] | --chrome [--url <url>] | --ie} " +
        	"[--include=<include file>] --script=<script file> [--quit] [--timeout={<seconds>|30}] [{--client=<server url>|--server=<port>}] <arg>..." );
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

    public static String getText( File file ) throws IOException {
    	return getText( new FileInputStream(file) );
    }
    
    public static String getText( InputStream is ) throws IOException {
    	 BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    	 return getText( reader );
    }
    
    public static String getText(BufferedReader reader) throws IOException {
        StringBuilder answer = new StringBuilder();
        // reading the content of the file within a char buffer
        // allow to keep the correct line endings
        char[] charBuffer = new char[8192];
        int nbCharRead /* = 0*/;
        try {
            while ((nbCharRead = reader.read(charBuffer)) != -1) {
                // appends buffer
                answer.append(charBuffer, 0, nbCharRead);
            }
            Reader temp = reader;
            reader = null;
            temp.close();
        } finally {
        	if( reader != null ) {
        		reader.close();
        	}
        }
        return answer.toString();
    }
}

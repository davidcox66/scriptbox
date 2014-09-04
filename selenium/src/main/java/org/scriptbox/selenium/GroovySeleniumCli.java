package org.scriptbox.selenium;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class GroovySeleniumCli {

	private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumCli.class );
	
    private static SeleniumController selenium;
    private static String includeText;
    private static GroovySeleniumMethods methods;
    
    public static SeleniumController getSelenium() {
    	return selenium;
    }
   
    public static String getIncludeText() {
    	return includeText;
    }
    
    public static GroovySeleniumMethods getMethods() {
    	return methods;
    }
    
    public static void main( String[] args ) {

    	try {
    		CommandLine cmd = new CommandLine( args );
    		
    		// just stripping this off if used in wrapper script for debug logging
    		cmd.consumeArg("debug"); 
    		cmd.consumeArg("trace"); 
    		
    		String scriptText = null;
    		List<String> parameters = null;
    		int server = cmd.consumeArgValueAsInt("server",false);
    		String client = cmd.consumeArgValue("client",false);
    				
    		if( server == 0 ) {
        		String script = cmd.consumeArgValue("script", true);
        		scriptText = getText( new File(script) );
        		parameters = cmd.getParameters();
    		}
    		if( client == null ) {
        		String include = cmd.consumeArgValue("include", false);
        		includeText = include != null ? getText(new File(include)) : null;

        		selenium = new SeleniumController( getDriverType(cmd) );
        		selenium.setTimeout( cmd.consumeArgValueAsInt( "timeout", false) );
        		selenium.setExe( cmd.consumeArgValue("exe",false) );
        		selenium.setProfile( cmd.consumeArgValue("profile", false) );
        		
        		methods = new GroovySeleniumMethods( selenium );
        		
        		String ru = cmd.consumeArgValue( "url", true);
        		try {
        			selenium.setUrl( new URL(ru) ); 
                }
                catch( Exception ex ) {
                	throw new RuntimeException( "Invalid remote url: '" + ru + "'", ex );
        		}
    		}
    	
    		cmd.checkUnusedArgs();

    		if( client != null ) {
    			HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
                factory.setServiceUrl("http://" + client  + "/remoting/selenium/");
                factory.setServiceInterface(GroovySeleniumRemote.class);
                factory.afterPropertiesSet();
                GroovySeleniumRemote remote = (GroovySeleniumRemote)factory.getObject();
                remote.run( scriptText, parameters ); 
    		}
    		else {
    			if( server != 0 ) {
    				JettyService jetty = new JettyService("classpath:selenium-context.xml");
    				jetty.setHttpPort( server );
    				jetty.start();
    				while( true ) {
    					System.out.print(".");
    					Thread.sleep( 60*1000 );
    				}
    			}
    			else {
    				try {
            			methods.run( scriptText, includeText, parameters);
    				}
    				finally {
        				selenium.disconnect();
    				}
    			}
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
    
    private static void usage() {
        System.err.println( "Usage: GroovySeleniumCli " +
        	"{--firefox [--profile <profile path>] | --chrome [--url <url>] | --ie} " +
        	"[--include=<include file>] --script=<script file> [--quit] [--timeout={<seconds>|30}] [{--client=<server url>|--server=<port>}] <arg>..." );
        System.exit( 1 );
    }

    private static SeleniumController.DriverType getDriverType( CommandLine cmd ) throws CommandLineException {
    	SeleniumController.DriverType[] types = SeleniumController.DriverType.values(); 
    	for( SeleniumController.DriverType type : types ) {
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

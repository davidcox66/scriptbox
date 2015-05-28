package org.scriptbox.selenium;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
		SeleniumController selenium = new SeleniumController( getDriverType(cmd) );
		selenium.setTimeout( cmd.consumeArgValueAsInt( "timeout", false) );
		selenium.setExe( cmd.consumeArgValue("exe",false) );
		selenium.setProfile( cmd.consumeArgValue("profile", false) );

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

	private static void setUrl( SeleniumController controller, String url ) {
		try {
			controller.setUrl(new URL(url));
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Invalid remote url: '" + url + "'", ex );
		}
	}

	private static void client( String serverHostPort, CommandLine cmd ) throws Exception {

        String include = cmd.consumeArgValue("include", false);
		String script = cmd.consumeArgValue("script", true);
        if( !script.endsWith(".groovy") ) {
            script += ".groovy";
        }

		List<String> parameters = cmd.getParameters();
		cmd.checkUnusedArgs();

		ClientSeleniumService client = new ClientSeleniumService( serverHostPort );
        GroovySeleniumShell methods = new GroovySeleniumShell( client );

		if( include != null ) {
            List<File> includes = getFileList( include );
            for (File file : includes) {
                LOGGER.trace( "client: running include=" + file );
                methods.run(file, parameters);
            }
        }
        File file = new File( script );
        LOGGER.debug( "trace: running script=" + file );
        methods.run( file, parameters );
	}

	private static List<File> getFileList( String filespec ) {
		List<File> ret = new ArrayList<File>();
        String[] specs = filespec.split(",");
        for( String spec : specs ) {
            File file = new File( spec );
            String name = file.getName();
            if( name.indexOf("*") == -1 && name.indexOf("?") == -1 )	 {
                if( file.isDirectory() ) {
                    ret.addAll( Arrays.asList(file.listFiles((FileFilter)new SuffixFileFilter(".groovy"))) );
                }
                else {
                    if( !name.endsWith(".groovy") ) {
                        file = new File( spec + ".groovy" );
                    }
                    if( file.exists() ) {
                        ret.add(file);
                    }
                    else {
                        throw new RuntimeException( "Could not find file: " + file );
                    }
                }
            }
            else {
                File parent = file.getParentFile();
                if( parent == null ) {
                    parent = new File( "." );
                }
                ret.addAll( Arrays.asList(parent.listFiles(
                    (FileFilter)new AndFileFilter(new WildcardFileFilter(name),new SuffixFileFilter(".groovy")))) );
            }
        }
		return ret;
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

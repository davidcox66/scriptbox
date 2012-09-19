package org.scriptbox.panopticon.main;

import java.io.File;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.scriptbox.panopticon.main.MonitorInterface;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.common.io.IoUtil;

public class MonitorCli {

	public static void main( String[] args ) {
	
		try {
			CommandLine cmd = new CommandLine( args );
			String host = cmd.consumeArgValue( "host", true );
			int port = cmd.consumeArgValueAsInt( "port", true );
			System.setProperty( "monitor.host", host );
			System.setProperty( "monitor.port", ""+port );
			ApplicationContext ctx = new ClassPathXmlApplicationContext( "classpath:panopticon-client-context.xml" );	
			MonitorInterface monitor = ctx.getBean( "monitor", MonitorInterface.class );
			if( cmd.consumeArgWithParameters("createContext", 2) ) {
				cmd.checkUnusedArgs();
				monitor.createContext(cmd.getParameter(0), cmd.getParameter(1) );
			}
			else if( cmd.consumeArgWithParameters("startContext",1) ) {
				cmd.checkUnusedArgs();
				monitor.startContext( cmd.getParameter(0) );
			}
			else if( cmd.consumeArgWithParameters("stopContext",1) ) {
				cmd.checkUnusedArgs();
				monitor.stopContext( cmd.getParameter(0) );
			}
			else if( cmd.consumeArgWithParameters("shutdownContext",1) ) {
				cmd.checkUnusedArgs();
				monitor.shutdownContext( cmd.getParameter(0) );
			}
			else if( cmd.consumeArgWithParameters("shutdownAllContext",0) ) {
				cmd.checkUnusedArgs();
				monitor.shutdownAllContexts();
			}
			else if( cmd.consumeArgWithParameters("loadScript",2,-1) ) {
				cmd.checkUnusedArgs();
				monitor.loadScript( cmd.getParameter(0), cmd.getParameter(1), IoUtil.readFile(new File(cmd.getParameter(2))), new String[] {} );
			}
			else {
				usage();
			}
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			usage();
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
	
	public static void usage() {
		System.err.println( "Usage: MonitorCli --host=<host> --port=<port>\n" +
			"    {\n" +
			"        --createContext <language> <contextName>\n" +
			"        --startContext <name>\n" +
			"        --stopContext <name>\n" +
			"        --shutdownContext <name>\n" +
			"        --shutdownAllContexts\n" +
			"        --loadScript <contextName> <scriptName> <script> <args...>\n" + 
			"    }" );
		System.exit( 1 );
	}
}

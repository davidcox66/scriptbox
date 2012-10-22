package org.scriptbox.panopticon.main;

import org.scriptbox.box.remoting.server.BoxServerCliHelper;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;

public class MonitorMain {

	public static void main( String[] args ) {
		
		try {
			CommandLine cmd = new CommandLine( args ) ;
			BoxServerCliHelper.consumeMainArgs( cmd, 7800 );
			BoxServerCliHelper.consumeDatabaseArgs( cmd );
			BoxServerCliHelper.consumeTunnelArgs( cmd );
			cmd.checkUnusedArgs();
			
			JettyService jetty = new JettyService( "classpath:panopticon-context.xml" );
			jetty.start();
			while( true ) {
				System.out.print( "." );
				Thread.sleep( 60*1000 );
			}
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			usage();
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
			System.exit( 1 );
		}
	}

	private static void usage() {
		BoxServerCliHelper.usage( "MonitorMain", 7800 );
		System.exit( 1 );
	}
}

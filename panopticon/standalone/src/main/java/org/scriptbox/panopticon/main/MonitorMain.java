package org.scriptbox.panopticon.main;

import org.scriptbox.box.remoting.server.BoxServerCliHelper;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;

public class MonitorMain {

	public static void main( String[] args ) {
		
		try {
			CommandLine cmd = new CommandLine( args ) ;
			JettyService jetty = BoxServerCliHelper.consumeAllArgs( cmd, "panopticon", 7800 );
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

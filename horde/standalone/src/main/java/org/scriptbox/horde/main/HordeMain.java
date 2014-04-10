package org.scriptbox.horde.main;

import org.scriptbox.box.remoting.server.BoxServerCliHelper;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;


public class HordeMain {

	public static void main( String[] args ) {
		
		try {
			CommandLine cmd = new CommandLine( args ) ;
			BoxServerCliHelper.consumeAllArgs( cmd, 7900 );
			JettyService jetty = new JettyService( "classpath:horde-context.xml" );
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
		BoxServerCliHelper.usage( "HordeMain", 7900 );
		System.exit( 1 );
	}
}

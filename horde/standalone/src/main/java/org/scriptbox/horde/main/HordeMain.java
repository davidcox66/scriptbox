package org.scriptbox.horde.main;

import org.scriptbox.util.remoting.jetty.JettyService;

public class HordeMain {

	public static void main( String[] args ) {
		
		try {
			JettyService jetty = new JettyService( "classpath:horde-context.xml" );
			jetty.start();
			while( true ) {
				System.out.print( "." );
				Thread.sleep( 60*1000 );
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
}

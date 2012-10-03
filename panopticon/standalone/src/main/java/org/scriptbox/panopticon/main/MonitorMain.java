package org.scriptbox.panopticon.main;

import org.scriptbox.util.remoting.jetty.JettyService;

public class MonitorMain {

	public static void main( String[] args ) {
		
		try {
			JettyService jetty = new JettyService( "classpath:panopticon-context.xml" );
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

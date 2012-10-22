package org.scriptbox.panopticon.main;

import org.scriptbox.box.remoting.client.BoxClientCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class MonitorCli {

	public static void main( String[] args ) {
	
		try {
			BoxClientCliHelper helper = new BoxClientCliHelper( args, "agent", new String[] { "classpath:panopticon-client-context.xml" }, 7800 );
			helper.process();
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			BoxClientCliHelper.usage();
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
}

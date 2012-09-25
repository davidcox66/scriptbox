package org.scriptbox.panopticon.main;

import org.scriptbox.box.remoting.BoxCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class MonitorCli {

	public static void main( String[] args ) {
	
		try {
			BoxCliHelper helper = new BoxCliHelper( args, "agent", new String[] { "classpath:panopticon-client-context.xml" }, 7800 );
			helper.process();
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			BoxCliHelper.usage();
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
}

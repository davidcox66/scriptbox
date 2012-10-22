package org.scriptbox.panopticon.main;

import org.scriptbox.box.remoting.client.BoxClientCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class MonitorCli {

	public static void main( String[] args ) {
	
		try {
			BoxClientCliHelper helper = new BoxClientCliHelper( "MonitorCli", args, 7800 );
			helper.process();
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			BoxClientCliHelper.usage("MonitorCli");
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
}

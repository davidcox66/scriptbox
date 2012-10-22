package org.scriptbox.horde.main;

import org.scriptbox.box.remoting.client.BoxClientCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class HordeCli {

	public static void main( String[] args ) {
	
		try {
			BoxClientCliHelper helper = new BoxClientCliHelper( args, "agent", new String[] { "classpath:horde-client-context.xml" }, 7900 );
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

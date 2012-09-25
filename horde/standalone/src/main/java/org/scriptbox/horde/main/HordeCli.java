package org.scriptbox.horde.main;

import org.scriptbox.box.remoting.BoxCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class HordeCli {

	public static void main( String[] args ) {
	
		try {
			BoxCliHelper helper = new BoxCliHelper( args, "agent", new String[] { "classpath:horde-client-context.xml" }, 7900 );
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

package org.scriptbox.horde.main;

import org.scriptbox.box.remoting.client.BoxClientCliHelper;
import org.scriptbox.util.common.args.CommandLineException;

public class HordeCli {

	public static void main( String[] args ) {
	
		try {
			BoxClientCliHelper helper = new BoxClientCliHelper( "HordeCli", args, 7900, 
				new String[] { "classpath:horde-client-context.xml" } );
			helper.process();
		}
		catch( CommandLineException ex ) {
			System.err.println( ex.getMessage() );
			BoxClientCliHelper.usage("HordeCli");
		}
		catch( Exception ex ) {
			ex.printStackTrace( System.err );
		}
	}
}

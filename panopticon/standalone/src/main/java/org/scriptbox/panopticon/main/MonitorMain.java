package org.scriptbox.panopticon.main;

import org.scriptbox.util.remoting.jetty.JettyService;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.apache.commons.lang.StringUtils;

public class MonitorMain {

	public static void main( String[] args ) {
		
		try {
			CommandLine cmd = new CommandLine( args ) ;
			String instance = cmd.consumeArgValue( "instance", true );
			String address = cmd.consumeArgValue( "address", "localhost" );
			int port = cmd.consumeArgValueAsInt( "port", 7800 );
			int jmxport = cmd.consumeArgValueAsInt( "jmxport", port+1 );
			String db = cmd.consumeArgValue( "db", "localhost" );
			String tags = cmd.consumeArgValue( "tags", "local" );
			
			System.setProperty( "metrics.instance", instance );
			System.setProperty( "jetty.hostname", address );
			System.setProperty( "jetty.port", ""+port );
			System.setProperty( "cassandra.host", db );
			System.setProperty( "heartbeat.tags", tags );
			System.setProperty( "com.sun.management.jmxremote.port", ""+jmxport );
			
			String sshhost = cmd.consumeArgValue( "sshhost", false );
			int sshport = cmd.consumeArgValueAsInt( "sshport", 22 );
			if( StringUtils.isNotEmpty(sshhost) ) {
				System.setProperty( "ssh.host", sshhost );
				System.setProperty( "ssh.port", ""+sshport );
			}
			cmd.checkUnusedArgs();
			
			JettyService jetty = new JettyService( "classpath:panopticon-context.xml" );
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
		System.err.println( "Usage: MonitorMain --instance=<instance> [--address=[localhost]] [--port=[7800]] [--db=[localhost]] " +
			"[--tags=<tag1,tag2,...>] [--sshhost=<host> [--sshport=[22]] [--jmxport=<port>]");
		System.exit( 1 );
	}
}

package org.scriptbox.box.remoting.server;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;

public class BoxServerCliHelper {

	public static void consumeMainArgs( CommandLine cmd, int defaultPort ) throws CommandLineException {
		String instance = cmd.consumeArgValue( "instance", true );
		String address = cmd.consumeArgValue( "address", "localhost" );
		int port = cmd.consumeArgValueAsInt( "port", defaultPort );
		System.setProperty( "box.instance", instance );
		System.setProperty( "jetty.hostname", address );
		System.setProperty( "jetty.port", ""+port );
		
		String tags = cmd.consumeArgValue( "tags", "local" );
		System.setProperty( "heartbeat.tags", tags );
		
		int jmxport = cmd.consumeArgValueAsInt( "jmxport", port+1 );
		System.setProperty( "com.sun.management.jmxremote.port", ""+jmxport );
	}
	
	public static void consumeDatabaseArgs( CommandLine cmd ) throws CommandLineException {
		String db = cmd.consumeArgValue( "db", "localhost" );
		System.setProperty( "cassandra.host", db );
		
		String dbTunnelHost = cmd.consumeArgValue("db-tunnel-host", false );
		if( StringUtils.isNotEmpty(dbTunnelHost) ) {
			int dbTunnelPort = cmd.consumeArgValueAsInt( "db-tunnel-port", 22);
			String dbTunnelUser = cmd.consumeArgValue( "db-tunnel-user", false );
			String dbTunnelPassword = cmd.consumeArgValue( "db-tunnel-password", StringUtils.isNotEmpty(dbTunnelUser) );
			
			System.setProperty("cassandra.tunnel.host", dbTunnelHost );
			System.setProperty("cassandra.tunnel.port", ""+dbTunnelPort );
			System.setProperty("cassandra.tunnel.user", dbTunnelUser );
			System.setProperty("cassandra.tunnel.password", dbTunnelPassword );
		}
	}
	
	public static void consumeTunnelArgs( CommandLine cmd ) throws CommandLineException { 
		String sshhost = cmd.consumeArgValue( "ssh-host", false );
		if( StringUtils.isNotEmpty(sshhost) ) {
			int sshport = cmd.consumeArgValueAsInt( "ssh-port", 22 );
			String user = cmd.consumeArgValue( "ssh-user", false );
			String password = cmd.consumeArgValue("ssh-password", StringUtils.isNotEmpty(user) );
			
			System.setProperty( "ssh.host", sshhost );
			System.setProperty( "ssh.port", ""+sshport );
			System.setProperty( "ssh.user", user );
			System.setProperty( "ssh.password", password );
		}
	
	}
	public static void usage( String name, int defaultPort ) {
		System.err.println( "Usage: " + name + " \n" +
			"\t--instance=<instance> [--address=[localhost]] [--port=[" + defaultPort + "]] \n" +
			"\t[--db=[localhost] [--db-tunnel-host=<host> --db-tunnel-port=<port>] [-db-tunnel-user=<user> -db-tunnel-password=<password>] \n" +
			"\t[--tags=<tag1,tag2,...>] [--ssh-host=<host> [--ssh-port=[22]] [--ssh-user=<user> --ssh-password=<password>]\n" +
			"\t[--jmxport=[port+1]]");
		System.exit( 1 );
	}
}

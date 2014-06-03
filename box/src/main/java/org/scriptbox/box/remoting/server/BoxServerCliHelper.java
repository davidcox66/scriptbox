package org.scriptbox.box.remoting.server;

import java.io.Console;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.jetty.JettyService;

public class BoxServerCliHelper {

	public static JettyService consumeAllArgs( CommandLine cmd, String baseName, int defaultPort ) throws CommandLineException {
		JettyService jetty = consumeMainArgs(cmd, baseName, defaultPort);
		consumeDatabaseArgs(cmd);
		consumeTunnelArgs(cmd);
		cmd.checkUnusedArgs();
		return jetty;
	}
	
	public static JettyService consumeMainArgs( CommandLine cmd, String baseName, int defaultPort ) throws CommandLineException {
		
		// handled by shell wrapper script		
		cmd.consumeArg( "debug" );
		cmd.consumeArg( "trace" );
		
		String address = cmd.consumeArgValue( "address", "localhost" );
		int port = cmd.consumeArgValueAsInt( "port", defaultPort );
		System.setProperty( "jetty.host", address );
		System.setProperty( "jetty.port", ""+port );
		
		String instance = cmd.consumeArgValue( "instance", true );
		System.setProperty( "box.instance", instance );
		
		String tags = cmd.consumeArgValue( "tags", "local" );
		System.setProperty( "heartbeat.tags", tags );
		
		int jmxport = cmd.consumeArgValueAsInt( "jmxport", port+1 );
		System.setProperty( "com.sun.management.jmxremote.port", ""+jmxport );
		
		String user = cmd.consumeArgValue( "user", false );
		String password = null;
		if( user != null ) {
			Console cons = System.console();
			if( cons == null ) {
				throw new CommandLineException( "Unable to read password from console" );
			}
			char[] passwd;
			if ((passwd = cons.readPassword("[%s]", "Password:")) != null) {
				password = new String( passwd );
			}
			if( StringUtils.isBlank(password) ) {
				throw new CommandLineException( "Must specify a password");
			}
			System.setProperty( "spring.user", user );
			System.setProperty( "spring.password", password );
		}
		
		String springContext = cmd.consumeArgValue( "spring-context", false );
		if( springContext == null ) {
			springContext = "classpath:" + baseName + "-context.xml";
		}
		String springSecurityContext = null;
		if( !cmd.consumeArg("no-security") ) {
			springSecurityContext = cmd.consumeArgValue( "spring-security-context", false );
			if( springSecurityContext == null ) {
				springSecurityContext = "classpath:" + baseName + "-security-context.xml";
			}
		}
		
		JettyService jetty = new JettyService( springContext, springSecurityContext );
		jetty.setHostname( address );
		jetty.setPort( port );
		return jetty;
	}
	
	public static void consumeDatabaseArgs( CommandLine cmd ) throws CommandLineException {
		String db = cmd.consumeArgValue( "db", "localhost" );
		System.setProperty( "cassandra.host", db );
		
		String dbTunnelHost = cmd.consumeArgValue("db-tunnel-host", "" );
		int dbTunnelPort = 0;
		String dbTunnelUser = "";
		String dbTunnelPassword = "";
		if( StringUtils.isNotEmpty(dbTunnelHost) ) {
			dbTunnelPort = cmd.consumeArgValueAsInt( "db-tunnel-port", 22);
			dbTunnelUser = cmd.consumeArgValue( "db-tunnel-user", false );
			dbTunnelPassword = cmd.consumeArgValue( "db-tunnel-password", StringUtils.isNotEmpty(dbTunnelUser) );
		}
		System.setProperty("cassandra.tunnel.host", dbTunnelHost );
		System.setProperty("cassandra.tunnel.port", ""+dbTunnelPort );
		System.setProperty("cassandra.tunnel.user", dbTunnelUser );
		System.setProperty("cassandra.tunnel.password", dbTunnelPassword );
	}
	
	public static void consumeTunnelArgs( CommandLine cmd ) throws CommandLineException { 
		String sshHost = cmd.consumeArgValue( "ssh-host", "" );
		int sshPort = 0;
		String user = "";
		String password = "";
		if( StringUtils.isNotEmpty(sshHost) ) {
			sshPort = cmd.consumeArgValueAsInt( "ssh-port", 22 );
			user = cmd.consumeArgValue( "ssh-user", false );
			password = cmd.consumeArgValue("ssh-password", StringUtils.isNotEmpty(user) );
			
		}
		System.setProperty( "ssh.host", sshHost );
		System.setProperty( "ssh.port", ""+sshPort );
		System.setProperty( "ssh.user", user );
		System.setProperty( "ssh.password", password );
	
	}
	public static void usage( String name, int defaultPort ) {
		System.err.println( "Usage: " + name + " \n" +
			"\t--instance=<instance> [--address=[localhost]] [--port=[" + defaultPort + "]] \n" +
			"\t[--db=[localhost] [--db-tunnel-host=<host> --db-tunnel-port=<port>] [-db-tunnel-user=<user> -db-tunnel-password=<password>] \n" +
			"\t[--tags=<tag1,tag2,...>] [--ssh-host=<host> [--ssh-port=[22]] [--ssh-user=<user> --ssh-password=<password>]\n" +
			"\t[--jmxport=[port+1]]\n" +
			"\t[--spring-context=<context file>]\n"  +
			"\t[--spring-security-context=<context file>]\n" +
			"\t[--no-security]\n" +
			"\t[--user=<user>] (prompts for password)" );
		System.exit( 1 );
	}
}

package org.scriptbox.box.remoting.server;

import java.io.Console;
import java.io.File;

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
	
	private static JettyService consumeMainArgs( CommandLine cmd, String baseName, int defaultPort ) throws CommandLineException {
		
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
		
		int jmxport = cmd.consumeArgValueAsInt( "jmxport", port+2 );
		System.setProperty( "com.sun.management.jmxremote.port", ""+jmxport );
		
		String user = cmd.consumeArgValue( "user", false );
		String password = null;
		if( user != null ) {
			password = getPassword( cmd );
			System.setProperty( "box.user", user );
			System.setProperty( "box.password", password );
		}
		
		JettyService jetty = new JettyService( 
			getSpringContextLocation(cmd,baseName), 
			getSecurityContextLocation(cmd,baseName,user,password) );
		
		jetty.setHostname( address );
		jetty.setKeyStorePath( cmd.consumeArgValue("keystore-path", false) );
		jetty.setKeyStorePassword(cmd.consumeArgValue("keystore-password", false) );
		jetty.setCertificateAlias(cmd.consumeArgValue("cert-alias", false) );
		jetty.setCertificatePassword(cmd.consumeArgValue("cert-password", false) );
		
		jetty.setHttpPort( port );
		jetty.setHttpsPort( port+1 );
		
		jetty.addDirectory( "/www", new File("/tmp/www") );
		
		String trustStore = cmd.consumeArgValue("trust-store", false );
		if( trustStore != null ) {
		    jetty.setTrustStorePath( trustStore );
		}
		String trustStorePassword = cmd.consumeArgValue("trust-store-password", false );
		if( trustStorePassword != null ) {
		    jetty.setTrustStorePassword( trustStorePassword );
		}
			    
		return jetty;
	}
	
	private static void consumeDatabaseArgs( CommandLine cmd ) throws CommandLineException {
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

	private static void consumeTunnelArgs( CommandLine cmd ) throws CommandLineException { 
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
	
	private static String getPassword( CommandLine cmd ) throws CommandLineException {
		
		String password = cmd.consumeArgValue( "password", false );
		if( password == null ) {
			Console cons = System.console();
			if( cons == null ) {
				throw new CommandLineException( "Unable to read password from console" );
			}
			char[] passwd;
			if ((passwd = cons.readPassword("%s", "Password:")) != null) {
				password = new String( passwd );
			}
			if( StringUtils.isBlank(password) ) {
				throw new CommandLineException( "Must specify a password");
			}
		}
		return password;
	}
	
	private static String getSpringContextLocation( CommandLine cmd, String baseName ) throws CommandLineException {
		String springContext = cmd.consumeArgValue( "spring-context", false );
		if( springContext == null ) {
			springContext = "classpath:" + baseName + "-context.xml";
		}
		return springContext;
	}
	
	private static String getSecurityContextLocation( CommandLine cmd, String baseName, String user, String password ) 
		throws CommandLineException
	{
		String springSecurityContext = cmd.consumeArgValue( "spring-security-context", false );
		if( springSecurityContext != null ) {
			if( springSecurityContext.equals("default") ) {
				springSecurityContext = "classpath:" + baseName + "-security-context.xml";
				if( user == null || password == null ) {
					throw new CommandLineException( "Must specify user and password when using default security context");
				}
			}
		}
		return springSecurityContext;
	}
	
	public static void usage( String name, int defaultPort ) {
		System.err.println( "Usage: " + name + " \n" +
			"\t--instance=<instance> [--address=[localhost]] [--port=[" + defaultPort + "]] \n" +
			"\t[--db=[localhost] [--db-tunnel-host=<host> --db-tunnel-port=<port>] [-db-tunnel-user=<user> -db-tunnel-password=<password>] \n" +
			"\t[--tags=<tag1,tag2,...>] [--ssh-host=<host> [--ssh-port=[22]] [--ssh-user=<user> --ssh-password=<password>]\n" +
			"\t[--jmxport=[port+2]]\n" +
			"\t[--spring-context=<context file>]\n"  +
			"\t[--spring-security-context=<context file>|'default']\n" +
			"\t[--user=<user> {--password=<password>|(prompts for password)}]\n" +
			"\t[--keystore=<keystore>] [--keystore-alias=<alias>] [--keystore-pass=<password>] [--key-pass=<pass>]\n"  +
			"\t[--trust-store=<location>] [--trust-store-password=<password>]" );
			
		System.exit( 1 );
	}
}

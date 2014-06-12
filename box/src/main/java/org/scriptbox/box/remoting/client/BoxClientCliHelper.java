package org.scriptbox.box.remoting.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.scriptbox.util.remoting.endpoint.EndpointConnectionFactory;
import org.scriptbox.util.remoting.endpoint.SshTunnelEndpoint;
import org.scriptbox.util.remoting.endpoint.TcpEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BoxClientCliHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxClientCliHelper.class );

	private String name;
	private int defaultAgentPort;
	private CommandLine cmd;

	private String tunnelHost;
	private int tunnelPort;
	private String tunnelUser;
	private String tunnelPassword;
	private String user;
	private String password;
	private boolean ssl;
	private BoxAgentHelper agentHelper;
	
	public BoxClientCliHelper( String name, String[] args, int defaultAgentPort, String[] contextConfigLocations ) throws CommandLineException {
		this.name = name;
		this.defaultAgentPort = defaultAgentPort;
	
		cmd = new CommandLine( args );
		
		user = cmd.consumeArgValue("user", false);
		if( user != null ) {
			password = cmd.consumeArgValue("password", true);
			System.setProperty( "box.user", user );
			System.setProperty( "box.password", password );
		}
		ssl = cmd.consumeArg("ssl"); 
		System.setProperty( "box.scheme", ssl ? "https" : "http" );
		
		if( ssl ) {
			String trustStore = cmd.consumeArgValue("trust-store", false );
			if( trustStore == null ) {
				File file = new File( System.getProperty("user.home") + File.separator + ".scriptbox" + File.separator + "truststore" );
				if( file.exists() ) {
					trustStore = file.getAbsolutePath();
				}
				else {
					throw new CommandLineException( "No trust store specified and the scriptbox default one does not exist in ~/.scriptbox/truststore");
				}
			}
		
			String trustStorePassword = cmd.consumeArgValue("trust-store-password", false );
			if( trustStorePassword == null ) {
				trustStorePassword = "password";
			}
		    System.setProperty( "javax.net.ssl.trustStore", trustStore );
		    System.setProperty( "javax.net.ssl.trustStorePassword", trustStorePassword );
		}
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext( contextConfigLocations );
		agentHelper = new BoxAgentHelper();
		agentHelper.setStreamFactory( ctx.getBean("streamFactory",PrintStreamFactory.class) );
		agentHelper.setEndpointConnectionFactory( ctx.getBean("connectionFactory",EndpointConnectionFactory.class) );
		
		// handled by shell wrapper script
		cmd.consumeArg( "debug" );
		cmd.consumeArg( "trace" );
		
		processTunnel();
		agentHelper.setEndpoints( getEndpoints() );
		
	}

	public static void usage( String name ) {
		System.err.println( "Usage: " + name + " [--port=<port>] [--tunnel=<host:[port]> --tunnel-user=<user> --tunnel-password=<password>] --agents=<[host]:[port]> ...\n" +
			"    [--ssl] [--user=<user> [--password=<password>]]\n" +
			"    [--trust-store=<location>] [--trust-store-password=<password>]\n" +
			"    [--debug] [--trace]\n" +
			"    {\n" +
			"        --status\n" +
			"        --createContext <language> <contextName>\n" +
			"        --startContext <contextName> <args...>\n" +
			"        --stopContext <contextName>\n" +
			"        --shutdownContext <contextName>\n" +
			"        --shutdownAllContexts\n" +
			"        --loadScript <contextName> <scriptName> <scriptFile> <args...>\n" + 
			"        --startScript <language> <contextName> <scriptName> <script> <args...>\n" + 
			"    }" );
		System.exit( 1 );
	}
	
	public void process() throws Exception {
		if( cmd.consumeArg("commands") ) {
			BufferedReader in = new BufferedReader( new InputStreamReader(System.in) );
			String line = null;
			while( (line = in.readLine()) != null ) {
				String[] args = line.split( "\\s+" );
				CommandLine cl = new CommandLine( args );
				processAgents( cl );
				agentHelper.join();
			}
		}
		else {
			processAgents( cmd );
			agentHelper.join();
		}
		
	}
	
	private void processTunnel() throws CommandLineException {
		String tunnel = cmd.consumeArgValue("tunnel",false);
		if( tunnel != null ) {
			String[] parts = tunnel.split(":");
			tunnelHost = parts[0];
			tunnelPort = parts.length > 1 ? Integer.parseInt(parts[1]) : 22;
			tunnelUser = cmd.consumeArgValue("user", true);
			tunnelPassword = cmd.consumeArgValue("password", true);
		}
	}
	
	private void processAgents( CommandLine cl ) throws Exception {
	
		if( cl.consumeArgWithMinParameters("createContext", 2) ) {
			cl.checkUnusedArgs();
			final List<String> params = cl.getParameters();
			final String language = params.get( 0 );
			final String contextName = params.get( 1 );
			final Map<String,Object> properties = new HashMap<String,Object>();
			if( params.size() > 2 ) {
				for( String prop : params.subList(2,params.size()) ) {
					String[] parts = prop.split("=");
					if( parts.length != 2 ) {
						throw new CommandLineException( "Context properties must be in the form name=value");
					}
					properties.put( parts[0], parts[1] );
				}
			}
			agentHelper.createContext(language, contextName, properties);
		}
		else if( cl.consumeArgWithMinParameters("startContext",1) ) {
			cl.checkUnusedArgs();
			List<String> parameters = cl.getParameters();
			final String contextName = parameters.get(0);
			final List<String> arguments = parameters.subList(1, parameters.size());
			agentHelper.startContext(contextName, arguments);
		}
		else if( cl.consumeArgWithParameters("stopContext",1) ) {
			cl.checkUnusedArgs();
			final String contextName = cl.getParameter( 0 );
			agentHelper.stopContext(contextName);
		}
		else if( cl.consumeArgWithParameters("shutdownContext",1) ) {
			cl.checkUnusedArgs();
			final String contextName = cl.getParameter( 0 );
			agentHelper.shutdownContext(contextName);
		}
		else if( cl.consumeArgWithParameters("shutdownAllContexts",0) ) {
			cl.checkUnusedArgs();
			agentHelper.shutdownAllContexts();
		}
		else if( cl.consumeArgWithMinParameters("loadScript",3) ) {
			cl.checkUnusedArgs();
			List<String> parameters = cl.getParameters();
			final String contextName = parameters.get(0);
			final String scriptName = parameters.get(1);
			final String fileName = parameters.get(2);
			final List<String> arguments = parameters.subList(3, parameters.size());
			agentHelper.loadScript(contextName, scriptName, fileName, arguments);
		}
		else if( cl.consumeArgWithMinParameters("startScript",4) ) {
			cl.checkUnusedArgs();
			List<String> parameters = cl.getParameters();
			final String language = parameters.get(0);
			final String contextName = parameters.get(1);
			final String scriptName = parameters.get(2);
			final String fileName = parameters.get(3);
			final List<String> arguments = parameters.subList(4, parameters.size());
			agentHelper.startScript(language, contextName, scriptName, fileName, arguments);
		}
		else if( cl.consumeArgWithParameters("status",0) ) {
			cl.checkUnusedArgs();
			agentHelper.status();
		}
		else {
			usage(name);
		}
	}
	
	private List<Endpoint> getEndpoints() throws CommandLineException {
		List<Endpoint> ret = new ArrayList<Endpoint>();
		
		int defaultPort = cmd.consumeArgValueAsInt("port", false);
		if( defaultPort == 0 ) {
			defaultPort = ssl ? defaultAgentPort+1 : defaultAgentPort;
		}
		
		String agents = cmd.consumeArgValue( "agents", true );
        String[] hostAndPorts = agents.split(",");
        for( String hostPort : hostAndPorts ) {
        	if( hostPort.indexOf(":") == 0 ) {
        		ret.add( createEndpoint("localhost",Integer.parseInt(hostPort.substring(1))) );
        	}
        	else {
	            String[] splitted = hostPort.split(":");
	            String host = splitted[0];
	            int port = splitted.length > 1 ? Integer.parseInt(splitted[1]) : defaultPort;
	            ret.add( createEndpoint(host,port) );
        	}
        } 
        return ret;
	}
	
	private Endpoint createEndpoint( String host, int port ) {
		if( tunnelHost == null ) {
			return new TcpEndpoint( host, port );
		}
		else {
			return new SshTunnelEndpoint( tunnelHost, tunnelPort, host, port, tunnelUser, tunnelPassword );
		}
	}
}

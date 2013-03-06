package org.scriptbox.box.remoting.client;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.scriptbox.util.remoting.endpoint.SshTunnelEndpoint;
import org.scriptbox.util.remoting.endpoint.TcpEndpoint;
import org.scriptbox.util.remoting.endpoint.EndpointConnectionFactory;
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
	private String user;
	private String password;
	private BoxAgentHelper agentHelper;
	
	public BoxClientCliHelper( String name, String[] args, int defaultAgentPort, String[] contextConfigLocations ) throws CommandLineException {
		this.name = name;
		this.defaultAgentPort = defaultAgentPort;
	
		ApplicationContext ctx = new ClassPathXmlApplicationContext( contextConfigLocations );
		agentHelper = new BoxAgentHelper();
		agentHelper.setStreamFactory( ctx.getBean("streamFactory",PrintStreamFactory.class) );
		agentHelper.setEndpointConnectionFactory( ctx.getBean("connectionFactory",EndpointConnectionFactory.class) );
		
		cmd = new CommandLine( args );
		
		// handled by shell wrapper script
		cmd.consumeArg( "debug" );
		cmd.consumeArg( "trace" );
		
		processTunnel();
		agentHelper.setEndpoints( getEndpoints() );
		
	}

	public static void usage( String name ) {
		System.err.println( "Usage: " + name + " [--port=<port>] [--tunnel=<host:[port]> --user <user> --password <password>] --agents=<[host]:[port]> ...\n" +
			"    --debug --trace\n" +
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
		processAgents();
		agentHelper.join();
	}
	
	private void processTunnel() throws CommandLineException {
		String tunnel = cmd.consumeArgValue("tunnel",false);
		if( tunnel != null ) {
			String[] parts = tunnel.split(":");
			tunnelHost = parts[0];
			tunnelPort = parts.length > 1 ? Integer.parseInt(parts[1]) : 22;
			user = cmd.consumeArgValue("user", true);
			password = cmd.consumeArgValue("password", true);
		}
	}
	
	private void processAgents() throws Exception {
	
		if( cmd.consumeArgWithParameters("createContext", 2) ) {
			cmd.checkUnusedArgs();
			final String language = cmd.getParameter( 0 );
			final String contextName = cmd.getParameter( 1 );
			agentHelper.createContext(language, contextName);
		}
		else if( cmd.consumeArgWithMinParameters("startContext",1) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String contextName = parameters.get(0);
			final List<String> arguments = parameters.subList(1, parameters.size());
			agentHelper.startContext(contextName, arguments);
		}
		else if( cmd.consumeArgWithParameters("stopContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			agentHelper.stopContext(contextName);
		}
		else if( cmd.consumeArgWithParameters("shutdownContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			agentHelper.shutdownContext(contextName);
		}
		else if( cmd.consumeArgWithParameters("shutdownAllContexts",0) ) {
			cmd.checkUnusedArgs();
			agentHelper.shutdownAllContexts();
		}
		else if( cmd.consumeArgWithMinParameters("loadScript",3) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String contextName = parameters.get(0);
			final String scriptName = parameters.get(1);
			final String fileName = parameters.get(2);
			final List<String> arguments = parameters.subList(3, parameters.size());
			agentHelper.loadScript(contextName, scriptName, fileName, arguments);
		}
		else if( cmd.consumeArgWithMinParameters("startScript",4) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String language = parameters.get(0);
			final String contextName = parameters.get(1);
			final String scriptName = parameters.get(2);
			final String fileName = parameters.get(3);
			final List<String> arguments = parameters.subList(4, parameters.size());
			agentHelper.startScript(language, contextName, scriptName, fileName, arguments);
		}
		else if( cmd.consumeArgWithParameters("status",0) ) {
			cmd.checkUnusedArgs();
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
			defaultPort = defaultAgentPort;
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
			return new SshTunnelEndpoint( tunnelHost, tunnelPort, host, port, user, password );
		}
	}
}

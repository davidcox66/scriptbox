package org.scriptbox.box.remoting;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.box.jmx.conn.HostPort;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.common.error.ExceptionHelper;
import org.scriptbox.util.common.io.IoUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.spring.context.ContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import org.scriptbox.util.remoting.tunnel.Tunnel;
import org.scriptbox.util.remoting.tunnel.TunnelCredentials;

public class BoxCliHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxCliHelper.class );
	
	private CommandLine cmd;
	private String agentBeanName;
	private String[] contextLocations;
	private List<HostPort> agents;
	private int defaultAgentPort;

	private String tunnelHost;
	private int tunnelPort;
	private TunnelCredentials credentials;
	
	public BoxCliHelper( String[] args, String agentBeanName, String[] contextLocations, int defaultAgentPort ) throws CommandLineException {
		this.agentBeanName = agentBeanName;
		this.contextLocations = contextLocations;
		this.defaultAgentPort = defaultAgentPort;
		cmd = new CommandLine( args );
		agents = getAgentHostPorts( cmd );
		processTunnel();
	}

	private void processTunnel() throws CommandLineException {
		String tunnel = cmd.consumeArgValue("tunnel",false);
		if( tunnel != null ) {
			String[] parts = tunnel.split(":");
			tunnelHost = parts[0];
			tunnelPort = parts.length > 1 ? Integer.parseInt(parts[1]) : 22;
			String user = cmd.consumeArgValue("user", true);
			String password = cmd.consumeArgValue("password", true);
			String passphrase = cmd.consumeArgValue("passphrase", false);
			credentials = new TunnelCredentials(user, passphrase, password);
		}
		
	}
	
	public void process() throws Exception {
		List<Thread> threads = processAgents();
		if( threads != null ) {
            for( Thread thread : threads ) {
                thread.join();
            }
		}
	}
	
	public static void usage() {
		System.err.println( "Usage: BoxCli [--port=<port>] [--tunnel=<host:[port]> --user <user> --password <password> [--passphrase <phrase>]] --agents=<[host]:[port]> ...\n" +
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
	
	private List<Thread> processAgents() throws Exception {
	
		if( cmd.consumeArgWithParameters("createContext", 2) ) {
			cmd.checkUnusedArgs();
			final String language = cmd.getParameter( 0 );
			final String contextName = cmd.getParameter( 1 );
			return forEachAgent( "Creating context", "Context created", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.createContext( language, contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithMinParameters("startContext",1) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String contextName = parameters.get(0);
			final List<String> arguments = parameters.subList(1, parameters.size());
			return forEachAgent( "Starting context", "Context started", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.startContext( contextName, arguments );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("stopContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			return forEachAgent( "Stopping context", "Context stopped", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.stopContext( contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("shutdownContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			return forEachAgent( "Shutting down context", "Context shutdown", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.shutdownContext( contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("shutdownAllContexts",0) ) {
			cmd.checkUnusedArgs();
			return forEachAgent( "Shutting down all contexts", "All contexts shutdown", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.shutdownAllContexts();
				}
			} );
		}
		else if( cmd.consumeArgWithMinParameters("loadScript",3) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String contextName = parameters.get(0);
			final String scriptName = parameters.get(1);
			final String fileName = parameters.get(2);
			final List<String> arguments = parameters.subList(3, parameters.size());
			return forEachAgent( "Loading script", "Script loaded", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.loadScript( contextName, scriptName, IoUtil.readFile(new File(fileName)), arguments  );
				}
			} );
		}
		else if( cmd.consumeArgWithMinParameters("startScript",4) ) {
			cmd.checkUnusedArgs();
			List<String> parameters = cmd.getParameters();
			final String language = parameters.get(0);
			final String contextName = parameters.get(1);
			final String scriptName = parameters.get(2);
			final String fileName = parameters.get(3);
			final List<String> arguments = parameters.subList(4, parameters.size());
			return forEachAgent( "Starting script", "Script started", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					agent.box.createContext( language, contextName );
					agent.box.loadScript( contextName, scriptName, IoUtil.readFile(new File(fileName)), arguments  );
					agent.box.startContext( contextName, arguments );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("status",0) ) {
			cmd.checkUnusedArgs();
			return forEachAgent( "Getting status", "Retrieved status", new ParameterizedRunnable<Agent>() {
				public void run( Agent agent ) throws Exception {
					System.out.println( prefixLines(agent.hostPort,agent.box.status()) );
				}
			} );
		}
		else {
			usage();
			return null;
		}
	}
	
	private List<Thread> forEachAgent( 
		String preMessage,
		String postMessage, 
		ParameterizedRunnable<Agent> runner )
			throws Exception
	{
		final List<Thread> ret = new ArrayList<Thread>();
		for( HostPort agent : agents ) {
			ret.add( invokeAgent(agent, runner, preMessage, postMessage) );
		}
		return ret;
	}
	
	private Thread invokeAgent( 
		final HostPort hostPort, 
		final ParameterizedRunnable<Agent> runner, 
		final String preMessage,
		final String postMessage ) 
	{
		Thread ret = new Thread(new Runnable() {
			public void run() {
				Tunnel tunnel = null;
	            try {
	            	HostPort hp = hostPort;
	            	if( credentials != null ) {
	            		tunnel = new Tunnel();
	            		tunnel.setCredentials( credentials );
	            		tunnel.setTunnelHost( tunnelHost );
	            		tunnel.setTunnelPort( tunnelPort );
	            		tunnel.setRemoteHost( hp.getHost() );
	            		tunnel.setRemotePort( hp.getPort() );
	            		int localPort = tunnel.connect();
	            		hp = new HostPort( "localhost", localPort );
	            		
	            		System.out.println(hostPort.getHost() + ":" + hostPort.getPort() + " : " +
	            			"established tunnel to: " + 
	            		    hostPort.getHost() + ":" + hostPort.getPort() + " through: " + localPort );
	            	}
					ApplicationContext ctx = ContextBuilder.create( agentBeanName, hp, contextLocations );	
					BoxInterface box = ctx.getBean( "monitor", BoxInterface.class );
					Agent agent = new Agent( hostPort, box );
					
			        System.out.println(hostPort.getHost() + ":" + hostPort.getPort() + " : " + preMessage + " ..." );
			        runner.run( agent );
	                if( postMessage != null ) {
	                	System.out.println( hostPort.getHost() + ":" + hostPort.getPort() + " : " + postMessage );
	                }
	            }
	            catch( Exception ex ) {
	                LOGGER.error( "Error from " + hostPort.getHost() + ":" + hostPort.getPort(), ex );
	                String exstr = ExceptionHelper.toString( ex );
	                System.out.println( prefixLines(hostPort,exstr) );
	            }
	            finally {
	            	if( tunnel != null ) {
	            		try {
		            		tunnel.disconnect();
	            		}
	            		catch( Exception ex ) {
	            			LOGGER.error( "Error disconnecting tunnel: " + tunnel.getRemoteHost() + ":" + tunnel.getRemotePort() );
	            		}
	            	}
	            }
			}
        }, hostPort.getHost() + ":" + hostPort.getPort() );
		ret.start();
		return ret;
	}
	
	private List<HostPort> getAgentHostPorts( CommandLine cmd ) throws CommandLineException {
		List<HostPort> ret = new ArrayList<HostPort>();
		
		int defaultPort = cmd.consumeArgValueAsInt("port", false);
		if( defaultPort == 0 ) {
			defaultPort = defaultAgentPort;
		}
		
		String agents = cmd.consumeArgValue( "agents", true );
        String[] hostAndPorts = agents.split(",");
        for( String hostPort : hostAndPorts ) {
        	if( hostPort.indexOf(":") == 0 ) {
        		ret.add( new HostPort("localhost",Integer.parseInt(hostPort.substring(1))) );
        	}
        	else {
	            String[] splitted = hostPort.split(":");
	            String host = splitted[0];
	            int port = splitted.length > 1 ? Integer.parseInt(splitted[1]) : defaultPort;
	            ret.add( new HostPort(host,port) );
        	}
        } 
        return ret;
	}

	private static String prefixLines( HostPort agent, String str ) {
        StringBuilder builder = new StringBuilder();
        String[] lines = str.split( "\n" );
        for( String line : lines ) {
            builder.append( agent.getHost() + ":" + agent.getPort() + " : " + line + "\n" );
        }
        return builder.toString();
	}
	
	private static class Agent
	{
		public HostPort hostPort;
		public BoxInterface box;
		
		public Agent( HostPort hostPort, BoxInterface box ) {
			this.hostPort = hostPort;
			this.box = box;
		}
	}
}

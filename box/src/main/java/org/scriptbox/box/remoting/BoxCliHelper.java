package org.scriptbox.box.remoting;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.scriptbox.box.plugins.jmx.HostPort;
import org.scriptbox.util.common.args.CommandLine;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.common.io.IoUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.spring.context.ContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BoxCliHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxCliHelper.class );
	
	private static final int DEFAULT_PORT = 7205;
	
	private CommandLine cmd;
	private String agentBeanName;
	private String[] contextLocations;
	private List<HostPort> agents;
	
	public BoxCliHelper( String[] args, String agentBeanName, String[] contextLocations ) throws CommandLineException {
		this.agentBeanName = agentBeanName;
		this.contextLocations = contextLocations;
		cmd = new CommandLine( args );
		agents = getAgentHostPorts( cmd );
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
		System.err.println( "Usage: BoxCli --agents=<host:[port]>...\n" +
			"    {\n" +
			"        --createContext <language> <contextName>\n" +
			"        --startContext <name>\n" +
			"        --stopContext <name>\n" +
			"        --shutdownContext <name>\n" +
			"        --shutdownAllContexts\n" +
			"        --loadScript <contextName> <scriptName> <script> <args...>\n" + 
			"    }" );
		System.exit( 1 );
	}
	
	private List<Thread> processAgents() throws Exception {
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext( "classpath:panopticon-client-context.xml" );	
		BoxInterface box = ctx.getBean( "monitor", BoxInterface.class );
		if( cmd.consumeArgWithParameters("createContext", 2) ) {
			cmd.checkUnusedArgs();
			final String language = cmd.getParameter( 0 );
			final String contextName = cmd.getParameter( 1 );
			return forEachAgent( "Creating context", "Context created", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.createContext( language, contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("startContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			return forEachAgent( "Starting context", "Context started", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.startContext( contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("stopContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 1 );
			return forEachAgent( "Stopping context", "Context stopped", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.stopContext( contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("shutdownContext",1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 1 );
			return forEachAgent( "Shutting down context", "Context shutdown", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.shutdownContext( contextName );
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("shutdownAllContext",0) ) {
			cmd.checkUnusedArgs();
			box.shutdownAllContexts();
			return forEachAgent( "Creating context", "Context created", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.shutdownAllContexts();
				}
			} );
		}
		else if( cmd.consumeArgWithParameters("loadScript",2,-1) ) {
			cmd.checkUnusedArgs();
			final String contextName = cmd.getParameter( 0 );
			final String scriptName = cmd.getParameter( 1 );
			final String fileName = cmd.getParameter( 2 );
			return forEachAgent( "Creating context", "Context created", new ParameterizedRunnable<BoxInterface>() {
				public void run( BoxInterface box ) throws Exception {
					box.loadScript( contextName, scriptName, IoUtil.readFile(new File(fileName)), new String[] {} );
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
		ParameterizedRunnable<BoxInterface> runner )
			throws Exception
	{
		final List<Thread> ret = new ArrayList<Thread>();
		for( HostPort agent : agents ) {
			ret.add( invokeAgent(agent, runner, preMessage, postMessage) );
		}
		return ret;
	}
	
	private Thread invokeAgent( 
		final HostPort agent, 
		final ParameterizedRunnable<BoxInterface> runner, 
		final String preMessage,
		final String postMessage ) 
	{
		final ApplicationContext ctx = ContextBuilder.create( agentBeanName, agent, contextLocations );	
		final BoxInterface box = ctx.getBean( "monitor", BoxInterface.class );
		return new Thread(new Runnable() {
			public void run() {
	            try {
			        System.out.println(agent.getHost() + ":" + agent.getPort() + " : " + preMessage + " ..." );
			        runner.run( box );
	                if( postMessage != null ) {
	                	System.out.println( agent.getHost() + ":" + agent.getPort() + " : " + postMessage );
	                }
	            }
	            catch( Exception ex ) {
	                LOGGER.error( "Error from " + agent.getHost() + ":" + agent.getPort(), ex );
	                String exstr = exceptionToString( ex );
	                StringBuilder builder = new StringBuilder();
	                String[] lines = exstr.split( "\n" );
	                for( String line : lines ) {
	                    builder.append( agent.getHost() + ":" + agent.getPort() + " : " + line + "\n" );
	                }
	                System.out.println( builder.toString() );
	            }
			}
        }, agent.getHost() + ":" + agent.getPort() );
	}
	
	private static List<HostPort> getAgentHostPorts( CommandLine cmd ) throws CommandLineException {
		List<HostPort> ret = new ArrayList<HostPort>();
		
		int defaultPort = cmd.consumeArgValueAsInt("port", false);
		if( defaultPort == 0 ) {
			defaultPort = DEFAULT_PORT;
		}
		
		String agents = cmd.consumeArgValue( "agents", true );
        String[] hostAndPorts = agents.split(",");
        for( String hostPort : hostAndPorts ) {
            String[] splitted = hostPort.split(":");
            String host = splitted[0];
            int port = splitted.length > 1 ? Integer.parseInt(splitted[1]) : defaultPort;
            ret.add( new HostPort(host,port) );
        } 
        return ret;
	}
	
	private static String exceptionToString( Exception ex ) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
  
}

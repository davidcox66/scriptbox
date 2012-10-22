package org.scriptbox.box.remoting.client;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.scriptbox.box.remoting.server.BoxInterface;
import org.scriptbox.util.common.args.CommandLineException;
import org.scriptbox.util.common.error.ExceptionHelper;
import org.scriptbox.util.common.io.IoUtil;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.remoting.endpoint.Connection;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.scriptbox.util.remoting.endpoint.EndpointConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxAgentHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxAgentHelper.class );

	private static ThreadLocal<PrintStream> outputs = new ThreadLocal<PrintStream>();
	
	private List<Endpoint> endpoints;
	private EndpointConnectionFactory<BoxInterface> factory;
	private List<Thread> threads = new ArrayList<Thread>();
	private PrintStreamFactory streams;
	
	public BoxAgentHelper( EndpointConnectionFactory<BoxInterface> factory, List<Endpoint> endpoints, PrintStreamFactory streams ) throws CommandLineException {
		this.factory = factory;
		this.endpoints = endpoints;
		this.streams = streams;
	}
	
	public void join() throws Exception {
		if( threads != null ) {
            for( Thread thread : threads ) {
                thread.join();
            }
		}
	}
	
	public void createContext( final String language, final String contextName ) throws Exception {
		forEachAgent( "Creating context", "Context created", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().createContext( language, contextName );
			}
		} );
	}

	public void startContext( final String contextName, final List arguments ) throws Exception {
		forEachAgent( "Starting context", "Context started", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().startContext( contextName, arguments );
			}
		} );
	}
	
	public void stopContext( final String contextName ) throws Exception {
		forEachAgent( "Stopping context", "Context stopped", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().stopContext( contextName );
			}
		} );
	}

	public void shutdownContext( final String contextName ) throws Exception { 
		forEachAgent( "Shutting down context", "Context shutdown", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().shutdownContext( contextName );
			}
		} );
	}
	
	public void shutdownAllContext() throws Exception {
		forEachAgent( "Shutting down all contexts", "All contexts shutdown", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().shutdownAllContexts();
			}
		} );
	}
	
	public void loadScript( final String contextName, final String scriptName, final String fileName, final List arguments ) throws Exception {
		forEachAgent( "Loading script", "Script loaded", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				conn.getProxy().loadScript( contextName, scriptName, IoUtil.readFile(new File(fileName)), arguments  );
			}
		} );
	}
	
	public void startScript( final String language, final String contextName, final String scriptName, final String fileName, final List arguments ) throws Exception {
		forEachAgent( "Starting script", "Script started", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				BoxInterface box = conn.getProxy();
				box.createContext( language, contextName );
				box.loadScript( contextName, scriptName, IoUtil.readFile(new File(fileName)), arguments  );
				box.startContext( contextName, arguments );
			}
		} );
	}
	
	public void status() throws Exception {
		forEachAgent( "Getting status", "Retrieved status", new ParameterizedRunnable<Connection<BoxInterface>>() {
			public void run( Connection<BoxInterface> conn ) throws Exception {
				outputs.get().println( conn.getProxy().status() );
			}
		} );
	}
	
	private void forEachAgent( 
		String preMessage,
		String postMessage, 
		ParameterizedRunnable<Connection<BoxInterface>> runner )
			throws Exception
	{
		for( Endpoint endpoint : endpoints ) {
			threads.add( invokeAgent(endpoint, runner, preMessage, postMessage) );
		}
	}
	
	private Thread invokeAgent( 
		final Endpoint endpoint, 
		final ParameterizedRunnable<Connection<BoxInterface>> runner, 
		final String preMessage,
		final String postMessage ) 
	{
		Thread ret = new Thread(new Runnable() {
			public void run() {
				Connection<BoxInterface> conn = null;
				PrintStream stream = streams.getOutputStream( endpoint );
				outputs.set( stream );
	            try {
					conn = factory.create( endpoint );
					if( conn == null ) {
						throw new Exception( "Could not create endpoint " + endpoint.getIdentifier() );
					}
			        stream.println( preMessage + " ..." );
			        runner.run( conn );
	                if( postMessage != null ) {
	                	stream.println( endpoint.getIdentifier() + " : " + postMessage );
	                }
	            }
	            catch( Exception ex ) {
	                LOGGER.error( "Error from " + endpoint.getIdentifier(), ex );
	                String exstr = ExceptionHelper.toString( ex );
	                stream.println( exstr );
	            }
	            finally {
	            	outputs.remove();
	            	if( conn != null ) {
	            		try {
		            		conn.close();
	            		}
	            		catch( Exception ex ) {
	            			LOGGER.error( "Error disconnecting: " + endpoint.getIdentifier() );
	            		}
	            	}
	            }
			}
        }, endpoint.getIdentifier() );
		ret.start();
		return ret;
	}
}

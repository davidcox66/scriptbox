package org.scriptbox.box.remoting.server;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scriptbox.box.container.Box;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.StatusProvider;
import org.scriptbox.box.controls.BoxService;
import org.scriptbox.box.controls.BoxServiceListener;
import org.scriptbox.box.controls.BoxServices;
import org.scriptbox.util.common.error.Errors;
import org.scriptbox.util.common.error.ExceptionHelper;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxWithServices implements BoxInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxWithServices.class );
	
	private Box box;

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public void start() throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "start: box=" + box ); }
		box.start();
		Collection<BoxServiceListener> listeners = box.getBeans().getAll(BoxServiceListener.class);
		BoxServices.start(getBoxServices(), null, listeners).raiseException( "Error starting services");
	}
	
	public void shutdown() throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "shutdown: box=" + box ); }
		Collection<BoxServiceListener> listeners = box.getBeans().getAll(BoxServiceListener.class);
		Errors<BoxService> errors = BoxServices.shutdown(getBoxServices(),listeners).logError( "Error stopping services", LOGGER );
		box.shutdown();
		errors.raiseException( "Error stopping services" );
	}
	
	@Override
	public void createContext(String language, String contextName) throws Exception {
		if( LOGGER.isDebugEnabled() ) { 
			LOGGER.debug( "createContext: box=" + box + ", language=" + language + ", contextName=" + contextName); 
		}
		BoxContext existing = box.getContext( contextName );
		if( existing != null ) {
			Collection<BoxServiceListener> listeners = existing.getBeans().getAll(BoxServiceListener.class);
			BoxServices.shutdown(getContextServices(contextName),listeners).raiseException( "Error shutting down services");
		}
		box.createContext( language, contextName );
	}

	@Override
	public void startContext(String contextName, List arguments ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { 
			LOGGER.debug( "startContext: box=" + box + ", contextName=" + contextName + ", arguments=" + arguments); 
		}
		Collection<BoxServiceListener> listeners = box.getContextEx(contextName).getBeans().getAll(BoxServiceListener.class);
		BoxServices.start(getContextServices(contextName), arguments,listeners).raiseException( "Error starting services");
	}
	
	@Override
	public void stopContext(String contextName ) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "stopContext: box=" + box + ", contextName=" + contextName); }
		Collection<BoxServiceListener> listeners = box.getContextEx(contextName).getBeans().getAll(BoxServiceListener.class);
		BoxServices.stop(getContextServices(contextName),listeners).raiseException( "Error suspending services");
	}

	@Override
	public void shutdownContext(String contextName) throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "shutdownContext: box=" + box + ", contextName=" + contextName); }
		Collection<BoxServiceListener> listeners = box.getContextEx(contextName).getBeans().getAll(BoxServiceListener.class);
		Errors<BoxService> errors = BoxServices.shutdown(getContextServices(contextName),listeners);
		errors.logError( "Error", LOGGER);
		box.shutdownContext( contextName );
		errors.raiseException( "Error shutting down services");
	}

	@Override
	public void shutdownAllContexts() throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "shutdownAllContexts: box=" + box ); }
		Errors<BoxService> errors = new Errors<BoxService>();
		for( BoxContext context : box.getContexts().values() ) {
			Collection<BoxServiceListener> listeners = context.getBeans().getAll(BoxServiceListener.class);
			errors.addAll( BoxServices.shutdown(context.getBeans().getAll(BoxService.class),listeners) );
		}
		errors.logError( "Error", LOGGER);
		box.shutdownAllContexts();
		errors.raiseException( "Error shutting down services");
	}

	@Override
	public void loadScript(String contextName, String scriptName, String script, List arguments) throws Exception{
		if( LOGGER.isDebugEnabled() ) { 
			LOGGER.debug( "loadScript: box=" + box + ", contextName=" + contextName + ", scriptName=" + script + ", arguments=" + arguments); 
		}
		box.run( contextName, scriptName, script, arguments );
	}

	public String status() throws Exception {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "status: box=" + box); }
		final StringBuilder builder = new StringBuilder( 1024 );
		Set<StatusProvider> bstats = box.getBeans().getAll( StatusProvider.class );
		if( bstats != null && bstats.size() > 0 ) {
			builder.append( "Box{\n" );
			buildStatus( builder, bstats );
			builder.append( "}\n" );
			
		}
		Map<String,BoxContext> contexts = box.getContexts();
		for( Map.Entry<String,BoxContext> entry : contexts.entrySet() ) {
			BoxContext.with(entry.getValue(), new ParameterizedRunnable<BoxContext>() {
				public void run( BoxContext context ) {
					Set<StatusProvider> cstats = context.getBeans().getAll(StatusProvider.class );
					builder.append( "Context[" + context.getName() + "] {\n" );
					buildStatus( builder, cstats );
					builder.append( "}" );
				}
			} );
		}
		return builder.toString();
	}
	
	private void buildStatus( StringBuilder builder, Set<StatusProvider> stats ) {
		if( stats != null && stats.size() > 0 ) {
			for( StatusProvider stat : stats ) {
				try {
					builder.append( "    " + stat.status() + "\n" );
				}
				catch( Exception ex ) {
					LOGGER.error( "Error getting status", ex );
					builder.append( ExceptionHelper.toString(ex) );
				}
			}
		}
	}
	
	private Set<BoxService> getBoxServices() {
		return box.getBeans().getAll(BoxService.class);
	}
	private Set<BoxService> getContextServices( String contextName ) {
		return  box.getContextEx(contextName).getBeans().getAll(BoxService.class);
	}
}

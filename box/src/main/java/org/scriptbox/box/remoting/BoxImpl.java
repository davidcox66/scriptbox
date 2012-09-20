package org.scriptbox.box.remoting;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.scriptbox.box.container.Box;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.controls.BoxService;
import org.scriptbox.box.controls.BoxServices;
import org.scriptbox.util.common.error.Errors;

public class BoxImpl implements BoxInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxImpl.class );
	
	private Box box;

	public Box getBox() {
		return box;
	}

	public void setBox(Box box) {
		this.box = box;
	}

	public void start() throws Exception {
		box.start();
		BoxServices.start(getBoxServices()).raiseException( "Error starting services");
	}
	
	public void shutdown() throws Exception {
		Errors<BoxService> errors = BoxServices.shutdown(getBoxServices()).logError( "Error stopping services", LOGGER );
		box.shutdown();
		errors.raiseException( "Error stopping services" );
	}
	
	@Override
	public void createContext(String language, String contextName) throws Exception {
		box.createContext( language, contextName );
	}

	@Override
	public void startContext(String contextName ) throws Exception {
		BoxServices.start(getContextServices(contextName)).raiseException( "Error starting services");
	}
	
	@Override
	public void stopContext(String contextName ) throws Exception {
		BoxServices.stop(getContextServices(contextName)).raiseException( "Error suspending services");
	}

	@Override
	public void shutdownContext(String contextName) throws Exception {
		Errors<BoxService> errors = BoxServices.shutdown(getContextServices(contextName));
		errors.logError( "Error", LOGGER);
		box.shutdownContext( contextName );
		errors.raiseException( "Error shutting down services");
	}

	@Override
	public void shutdownAllContexts() throws Exception {
		Errors<BoxService> errors = new Errors<BoxService>();
		for( BoxContext context : box.getContexts().values() ) {
			errors.addAll( BoxServices.shutdown(context.getBeans().getAll(BoxService.class)) );
		}
		errors.logError( "Error", LOGGER);
		box.shutdownAllContexts();
		errors.raiseException( "Error shutting down services");
	}

	@Override
	public void loadScript(String contextName, String scriptName, String script, String[] args) throws Exception{
		box.run( contextName, scriptName, script, args );
	}

	private Set<BoxService> getBoxServices() {
		return box.getBeans().getAll(BoxService.class);
	}
	private Set<BoxService> getContextServices( String contextName ) {
		return  box.getContextEx(contextName).getBeans().getAll(BoxService.class);
	}
}

package org.scriptbox.box.inject;

import java.util.List;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.BoxScript;
import org.scriptbox.box.events.BoxContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class BoxContextInjectingListener implements BoxContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxContextInjectingListener.class );
	
	private List<? extends Injector> injectors;
	
	public List<? extends Injector> getInjectors() {
		return injectors;
	}
	public void setInjectors(List<? extends Injector> injectors) {
		this.injectors = injectors;
	}


	@Override
	public void contextCreated(BoxContext context) throws Exception {
		if( injectors != null && injectors.size() > 0 ) {
			for( Injector injector : injectors ) {
				if( context.getLanguage().equals(injector.getLanguage()) ) {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "contextCreated: injecting " + injector + " for language: " + context.getLanguage()); }
					injector.inject( context );
				}
				else {
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "contextCreated: injector " + injector + " with language: " + injector.getLanguage() + 
						", does not match context language: " + context.getLanguage()); }
					
				}
			}
		}
		else {
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "contextCreated: no injectors defined"); }
		}
	}
	
	@Override
	public void contextShutdown(BoxContext context) throws Exception {
	}

	@Override
	public void executingScript(BoxScript script) {
	}

	public void finishedScript(BoxScript script) {
	}
}

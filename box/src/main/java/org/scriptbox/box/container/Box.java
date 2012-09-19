package org.scriptbox.box.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.events.BoxListener;
import org.scriptbox.box.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.collection.Iterators;
import org.scriptbox.util.common.collection.CollectionUtil;

public class Box {

    private static Logger LOGGER = LoggerFactory.getLogger( Box.class );
    
    private static final List<String> EMPTY = Collections.unmodifiableList( new ArrayList<String>() );
    
    private Map<String,BoxContext> contexts = new HashMap<String,BoxContext>(); 
    private List<BoxContextListener> contextListeners;
    private List<BoxListener> boxListeners;
    private List<Injector> injectors;
    
    private Lookup beans = new MapLookup();
    
    private BoxContextFactory contextFactory;
    
    public Box() {
    }
    
	public void start() throws Exception {
    	callListeners( new ParameterizedRunnable<BoxListener>() {
    		public void run( BoxListener listener ) throws Exception {
    			listener.createdBox( Box.this );
    		}
    	});
    }
    
    public void shutdown() throws Exception {
    	callListeners( new ParameterizedRunnable<BoxListener>() {
    		public void run( BoxListener listener ) throws Exception {
    			listener.shutdownBox( Box.this );
    		}
    	});
    }
    
	synchronized public void createContext( String language, String contextName ) throws Exception {
        if( contexts.get(contextName) != null ) {
            LOGGER.info( "createContext: alreadying loaded context " + contextName + ", will unload first" );
            shutdownContext( contextName );
        }
        LOGGER.info( "createContext: loading context: " + contextName );
        BoxContext context = contextFactory.create(this,language,contextName) ;
        context.setListeners( contextListeners );
        context.start();
        contexts.put( contextName, context );
        LOGGER.info( "createContext: finished loading: " + contextName );
    }
     
    synchronized public void shutdownContext( String contextName ) throws Exception {
    	try {
	        final BoxContext context = contexts.remove( contextName );
	        if( context != null ) {
		        LOGGER.info( "shutdownContext: stopping context=" + contextName );
		        context.shutdown();
		        LOGGER.info( "shutdownContext: stopped context=" + contextName );
	        }
	        else {
	            LOGGER.info( "shutdownContext: not loaded: ${contextName}");
	        }
        }
        finally {
        	cleanup();
        }
    }
    
    synchronized public void shutdownAllContexts() throws Exception {
    	try {
	        if( contexts != null ) {
		        LOGGER.info( "shutdownAllContexts: stopping...");
		        Iterators.callAndCollectExceptions(contexts.values(), new ParameterizedRunnable<BoxContext>() {
		        	public void run( BoxContext context ) throws Exception {
		        		context.shutdown();
		        	}
		        } ).raiseException("Error shutting down contexts");
		        LOGGER.info( "shutdownAllContexts: complete");
	        }
	        else {
	            LOGGER.info( "shutdown: nothing loaded");
	        }
    	}
        finally {
            contexts.clear();
            cleanup();
        }
    }

    synchronized public void run( String contextName, String name, String script, String[] args ) throws Exception {
    	BoxContext ctx = getContextEx( contextName );
        ctx.run( new BoxScript(ctx,name, script, args != null ? Arrays.asList(args) : EMPTY) ) ;
    }
    public BoxContext getContext( String name ) {
    	return contexts.get( name );
    }
   
    public BoxContext getContextEx( String name ) {
    	BoxContext ret = contexts.get( name );
    	if( ret == null ) {
    		throw new RuntimeException( "No context with name '" + name + "' is defined" );
    	}
    	return ret;
    }
    
    public Lookup getBeans() {
		return beans;
	}

	public BoxContextFactory getContextFactory() {
		return contextFactory;
	}

	public void setContextFactory(BoxContextFactory contextFactory) {
		this.contextFactory = contextFactory;
	}

	public Map<String, BoxContext> getContexts() {
		return contexts;
	}

	public <X extends Injector> List<X> getInjectorsOfType( Class<X> cls ) {
		List<X> ret =  CollectionUtil.getObjectsOfType( injectors, cls );
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getInjectorsOfType: class=" + cls + ", injectors=" + injectors + ", ret=" + ret ); }
		return ret;
	}
	
	public List<Injector> getInjectors() {
		return injectors;
	}

	public void setInjectors(List<Injector> injectors) {
		this.injectors = injectors;
	}
	public List<BoxContextListener> getContextListeners() {
		return contextListeners;
	}
	public void setContextListeners(List<BoxContextListener> contextListeners) {
		this.contextListeners = contextListeners;
	}
	public List<BoxListener> getBoxListeners() {
		return boxListeners;
	}
	public void setBoxListeners(List<BoxListener> boxListeners) {
		this.boxListeners = boxListeners;
	}

    private void cleanup() {
    	System.runFinalization();
    	System.gc();
    }
    
    private void callListeners( ParameterizedRunnable<BoxListener> runner ) throws Exception {
        Iterators.callAndCollectExceptions( boxListeners, runner ).raiseException( "Error calling listeners" );
     }
}

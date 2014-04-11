package org.scriptbox.horde.action;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.box.container.BoxScript;
import org.scriptbox.box.groovy.Closures;
import org.scriptbox.horde.metrics.ScriptMetric;
import org.scriptbox.horde.metrics.ThreadCount;
import org.scriptbox.metrics.beans.mbean.AbstractDynamicExposableMBean;
import org.scriptbox.metrics.beans.mbean.DynamicExposableMBean;
import org.scriptbox.util.common.obj.RunnableWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionScript {

    private static final Logger LOGGER = LoggerFactory.getLogger( ActionScript.class );
 
    private BoxScript boxScript;
    private List<Action> actions = new ArrayList<Action>(); 
    private List<ActionRunner> runners = new ArrayList<ActionRunner>();
    private List<Runnable> destructors = new ArrayList<Runnable>();
    private List<ActionErrorHandler> globalErrorHandlers = new ArrayList<ActionErrorHandler>();
    
    private AbstractDynamicExposableMBean mbean;
    
    /**
     * Glean the classloader from calls to addAction which should have the context classloader
     * from the scriptengine - the same classloader we want to use for other threads.
     */
    private ClassLoader scriptClassLoader;
    
    public ActionScript( BoxScript boxScript ) {
        this.boxScript = boxScript;
        this.mbean = new DynamicExposableMBean(getObjectName());
        addScriptMetric( new ThreadCount() );
    }
    
    public String getName() {
    	return boxScript.getName();
    }
    
    public List<Action> getActions() {
		return actions;
	}

	public List<ActionRunner> getRunners() {
		return runners;
	}

	public BoxScript getBoxScript() {
		return boxScript;
	}

	public ClassLoader getScriptClassLoader() {
		return scriptClassLoader;
	}

    synchronized public int getNumRunners() {
        return runners.size();
    }    
   
    public void addGlobalErrorHandler( ActionErrorHandler handler ) {
    	globalErrorHandlers.add( handler );
    }
    
	public void addAction( Action action ) {
    	if( scriptClassLoader == null ) {
    		scriptClassLoader = Thread.currentThread().getContextClassLoader();
    	}
        actions.add( action );
        if( LOGGER.isDebugEnabled() ) {
	        LOGGER.debug( "addAction: added action " + action.getName() );
        }
    }
   
    public void addScriptMetric( ScriptMetric metric ) { 
        metric.init( this );
        mbean.addExposable( metric );
    }

    // generic type should technically be ? extends GeneratorMetric but Groovy flips out 
    public void initialize() throws Exception {
    	Closures.callInClassLoader( scriptClassLoader, new RunnableWithException() {
    		public void run() throws Exception {
	            LOGGER.debug( "initialize: name=" + getName() );
	            try {
	            	for( Action action : actions ) {
	                    action.initialize();
	                } 
	                registerMBeans();
	            }
	            catch( Exception ex ) {
	                LOGGER.error( "Failed initializing, attempting cleanup...", ex );
	                cleanup();
	                throw ex;
	            }
	        }
    	} );
    }

    public void addDestructor( Runnable destroy ) {
    	destructors.add( destroy );
    } 

    public void callDestructors() {
    	for( Runnable destructor : destructors ) {
    		try {
	    		destructor.run();
    		}
    		catch( Throwable ex ) {
    			LOGGER.error( "Error from destructor", ex );
    		}
    	}
    }
    
    synchronized void start( int threadCount ) throws Exception {
    	if( actions.size() == 0 ) {
    		throw new RuntimeException( "No actions defined");
    	}
    	
        if( runners.size() > threadCount ) {
            int sz = runners.size();
            LOGGER.debug( "start: decreasing threads from " + sz + " to " + threadCount);
            for( int i=threadCount ; i < sz ; i++ ) {
                runners.get(i).stop();
            }
            while( runners.size() > threadCount ) {
                ActionRunner runner = runners.remove( runners.size() - 1);
                runner.join();
            }
        }
        else if( runners.size() < threadCount ) {
            LOGGER.debug( "start: increasing threads from " + runners.size() + " to " + threadCount);
            while( runners.size() < threadCount ) {
                ActionRunner runner = new ActionRunner(this);
                runners.add( runner );
                runner.start();
            }
        }
        else {
            LOGGER.debug( "start: no change from " + runners.size() + " threads");
        }
    } 
   
    synchronized void stop() throws Exception {
        LOGGER.debug( "stop: stopping " + runners.size() + " threads");
        List<Thread> joiners = new ArrayList<Thread>();
        try {
        	for( ActionRunner runner : runners ) {
	            runner.stop();
	        }
        	//
        	// May seem a little redundant kicking off threads to join but we want to wait a 
        	// reasonable amount of time for each to finish and then abort if they are
        	// unresponsive.
        	//
        	for( ActionRunner runner : runners ) {
        		joiners.add( runner.joinAsynchronously() );
	        }
        	for( Thread thread : joiners ) {
        		thread.join();
        	}
        }
        finally {
		    runners.clear();
        }
    }
   
    synchronized public void shutdown() throws Exception {
        LOGGER.debug( "shutdown: shutting down script");
        try {
            stop();
        }
        finally {
        	cleanup();
        }
    }
    
    private void cleanup() {
        try {
           unregisterMBeans(); 
        }
        catch( Exception ex ) {
            LOGGER.error( "Error unregistering MBeans", ex );
        }
        callDestructors();
    }
    
    private void registerMBeans() throws Exception {
    	mbean.register();
    	for( Action action : actions ) {
            action.registerMBeans();
        }
    }    
    
    private void unregisterMBeans() throws Exception {
    	try {
	    	mbean.unregister();
    	}
    	catch( Exception ex ) {
    		LOGGER.error( "Error unregistering: " + mbean.getObjectName() );
    	}
    	for( Action action : actions ) {
    		try {
	            action.unregisterMBeans();
    		}
	    	catch( Exception ex ) {
	    		LOGGER.error( "Error unregistering mbeans for action: " + action.getName() );
	    	}
        }
    } 

    public void callGlobalErrorHandlers( Throwable ex ) throws Throwable {
    	if( globalErrorHandlers.size() > 0 ) {
	    	for( ActionErrorHandler errorHandler : globalErrorHandlers ) {
	    		errorHandler.handle( ex );
	    	}
    	}
    	else {
    		throw ex;
    	}
    }
    
    /**
     * A runner stopping normally will have its running flag set to false and exit. One that
     * fails without the scripts knowledge or consent must be removed from the list manually.
     * This should only be the case in a catastrphic error.
     * @param runner
     */
    synchronized public void removeRunnerExplicity( ActionRunner runner ) {
        LOGGER.info( "removeRunnerExplicity: load runner removed manually: " + runner );
        runners.remove( runner );
    } 
   
    public String getObjectName() {
	    return "ActionMetrics:context=" + getBoxScript().getContext().getName() + ",script=" + getName();
    }
    
    public String toString() {
       return "ActionScript{ name=" + getName() + ", threads=" + runners.size() + "}"; 
    } 
}

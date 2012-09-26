package org.scriptbox.horde.action;

import org.scriptbox.box.groovy.Closures;
import org.scriptbox.util.common.obj.RunnableWithThrowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger( ActionRunner.class );
	
    private Thread thread;
    private boolean running=true;
    private ActionScript script;
    private ActionErrorHandler handler = new PauseActionErrorHandler( 2000 );
    
    public ActionRunner( ActionScript script ) {
        this.script = script;
    }
    
    public void start() {
        thread = new Thread(new Runnable() { 
        	public void run() {
	            try {
	            	Closures.callInClassLoader( script.getScriptClassLoader(), new RunnableWithThrowable() {
	            		public void run() throws Throwable {
			                while( running ) {
			                	Action aborted = null;
			                	try {
				                	for( Action action : script.getActions() ) {
						                try {
						                   action.callAllAndCollectMetrics();
						                }
						                catch( ActionScriptAbortException ex ) {
						                	aborted = action;
						                	throw ex;
						                }
				                        catch( VirtualMachineError ex ) {
				                            exitAbnormally( ex );    
				                            return;
				                        }
						                catch( Throwable ex ) {
						                   handler.handle( ex );
						                }
					                }
			                	}
			                	catch( ActionScriptAbortException ex ) {
			                		LOGGER.debug( "Action aborted script execution: " + aborted + " - " + ex.getMessage() );
			                	}
			                }
			                LOGGER.info( "Runner thread exiting normally: " + Thread.currentThread().getName() );
			            }
	            	} );
	            }
	            catch( Throwable ex ) {
	                exitAbnormally( ex );
	            }
	        }
        } );
        thread.start();
    }
    
    private void exitAbnormally( Throwable ex ) {
        Thread current = Thread.currentThread();
        LOGGER.error( "Runner thread exiting abnormally: " + current.getName(), ex );
        running = false;
        script.removeRunnerExplicity( this );
    } 
    
    void stop() {
        running = false;
    }
    void join() {
    	try {
	        thread.join( 10 * 1000 );
	        if( thread.isAlive() ) {
	            LOGGER.warn( "join: thread did not stop in allotted time, interrupting...");
	            thread.interrupt();
	        }
    	}
    	catch( InterruptedException ex ) {
    		LOGGER.warn( "join: interrupted while waiting for thread to finish", ex );
    	}
    }
}

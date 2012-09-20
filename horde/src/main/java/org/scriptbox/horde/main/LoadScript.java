package org.scriptbox.horde.main;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.scriptbox.horde.metrics.GeneratorMetric;
import org.scriptbox.horde.metrics.ScriptMetric;
import org.scriptbox.horde.metrics.TestMetric;
import org.scriptbox.horde.metrics.ThreadCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoadScript extends Scriptable {

    private static final Logger LOGGER = LoggerFactory.getLogger( LoadScript.class );
 
    private static ThreadLocal<Map<String,Object>> attributes = new ThreadLocal<Map<String,Object>>() {
        public Map<String,Object> initialValue() {
            return new HashMap<String,Object>();
        }
    };
     
    LoadContext context; 
     
    Closure destroy;
   
    List<Test> tests = new ArrayList<Test>(); 
    List<Runner> runners = new ArrayList<Runner>();
   
    Map<ObjectName,ScriptMetric> scriptMetrics = new HashMap<ObjectName,ScriptMetric>();
    
    class Test {
        String name;
        Runnable init;
        Runnable run;
        Runnable pre;
        Runnable post;
        
        Map<ObjectName,TestMetric> preMetrics = new HashMap<ObjectName,TestMetric>();
        Map<ObjectName,TestMetric> metrics = new HashMap<ObjectName,TestMetric>();
        Map<ObjectName,TestMetric> postMetrics = new HashMap<ObjectName,TestMetric>();
    
        Test( String name, Map params ) {
            this.name = name;
            this.run = (Runnable)params.get("run");
            this.init = (Runnable)params.get("init");
            this.pre = (Runnable)params.get("pre");
            this.post = (Runnable)params.get("post");
           
            if( run == null ) {  
                throw new Exception( "No run block defined for test");
            }
            
            if( run ) { run.setDelegate( this ); }
            if( init ) { init.setDelegate( this ); }
            if( pre ) { pre.setDelegate( this ); }
            if( post ) { post.setDelegate( this ); }
            
        }
        
        void initialize() {
            init?.call();
            if( !metrics && !preMetrics && !postMetrics )  {
                if( pre ) {
	                addPreMetric( new AvgTransactionTime('pre') );
                }
		        addRunMetric( new TransactionsPerSecond() );
		        addRunMetric( new TransactionCount() );
		        addRunMetric( new MinTransactionTime() );
		        addRunMetric( new MaxTransactionTime() );
		        addRunMetric( new AvgTransactionTime() );
		        addRunMetric( new FailureCount() );
                if( post ) {
	                addPostMetric( new AvgTransactionTime('post') );
                }
            }
        }
	    void addRunMetric( TestMetric metric ) {
            addTestMetric( metrics, metric );
        }
	    void addPreMetric( TestMetric metric ) {
            addTestMetric( preMetrics, metric );
        }
	    void addPostMetric( TestMetric metric ) {
            addTestMetric( postMetrics, metric );
        }
	    private void addTestMetric( Map<ObjectName,TestMetric> mets, TestMetric metric ) {
	        metric.init( LoadScript.this, this );
            addMetric( mets, metric );
	    }
       
        Object getTestAttribute( String name ) {
           return attributes.get().get( name ); 
        } 
        void setTestAttribute( String name, Object value ) {
            attributes.get().put( name, value );
        }

        void callAndCollectMetrics( List arguments, Map<ObjectName,TestMetric> met, Closure closure ) {
            if( closure ) {
	            long before = System.currentTimeMillis(); 
                try {
                   def ret = closure.getMaximumNumberOfParameters() > 0 ? closure.call(arguments) : closure.call();    
                   collectMetrics( met, ret ? true : false, System.currentTimeMillis() - before );
                }
                catch( VirtualMachineError ex ) {
                    throw ex;
                }
                catch( Throwable ex ) {
                   collectMetrics( met, false, System.currentTimeMillis() - before );
                   throw ex;
                }
            }
        }
        
	    void collectMetrics( boolean success, long millis ) {
            collectMetrics( metrics, success, millis );
	    }
	    void collectMetrics( Map<ObjectName,TestMetric> met, boolean success, long millis ) {
	        met.values().each{ TestMetric metric ->
	            metric.record( success, millis );
	        }
	    }
	    private void registerMBeans() {
            [preMetrics,metrics,postMetrics].each{ Map<ObjectName,TestMetric> met ->
		        met.values().each{ TestMetric metric ->
		            metric.register();
		        }
            }
	    }    
	    
	    private void unregisterMBeans() {
            [preMetrics,metrics,postMetrics].each{ Map<ObjectName,TestMetric> met ->
		        met.values().each{ TestMetric metric ->
	                try {
			            metric.unregister();
	                }
	                catch( Exception ex ) {
	                    LOGGER.warn( "unregisterMBeans: failed unregistering metric: ${metric}", ex );
	                }
		        }
            }
	    } 
    } 
    
    class Runner {
        
        private Thread thread;
        private boolean running=true;
        List arguments;
         
        Runner( List arguments ) {
            this.arguments = arguments;
        }
        
        void start() {
            thread = Thread.start{ 
                Thread current = Thread.currentThread();
                try {
	                current.setContextClassLoader( context.loader );
	                while( running ) {
	                    tests.each{ Test test ->
			                try {
                               test.callAndCollectMetrics( arguments, test.preMetrics, test.pre );
                               test.callAndCollectMetrics( arguments, test.metrics, test.run );
                               test.callAndCollectMetrics( arguments, test.postMetrics, test.post );
			                }
                            catch( VirtualMachineError ex ) {
                                exitAbnormally( ex );    
                                return;
                            }
			                catch( Throwable ex ) {
			                   LOGGER.error( "Error from load test, pausing momentarily: ${current.name} ...", ex ); 
			                   current.sleep( 2000 );
			                   LOGGER.info( "Resuming load test: ${current.name}", ex ); 
			                }
		                }
	                }
                    LOGGER.info( "Runner thread exiting normally: ${current.name}" );
                }
                catch( Throwable ex ) {
                    exitAbnormally( ex );
                }
            };
        }
        
        private void exitAbnormally( Throwable ex ) {
            Thread current = Thread.currentThread();
            LOGGER.error( "Runner thread exiting abnormally: ${current.name}", ex );
            running = false;
            removeRunnerExplicity( this );
        } 
        
	    void stop() {
	        running = false;
	    }
	    void join() {
	        thread.join( 10 * 1000 );
	        if( thread.isAlive() ) {
	            LOGGER.warn( "join: thread did not stop in allotted time, interrupting...");
	            thread.interrupt();
	        }
	    }
    }  
    
    LoadScript( LoadContext context, String name, String scriptText, List args ) {
        super( name, scriptText, args );
        this.context = context;
        addScriptMetric( new ThreadCount() );
    }
    
    void addScriptMetric( ScriptMetric metric ) { 
        metric.init( this );
        addMetric( scriptMetrics, metric );
    }
    
    // generic type should technically be ? extends GeneratorMetric but Groovy flips out 
    void addMetric( Map<ObjectName,GeneratorMetric> mets, GeneratorMetric metric ) { 
        ObjectName objectName = metric.objectName;
        LOGGER.debug( "addMetric: metric=${metric}, objectName=${objectName}" );
        ScriptMetric existing = mets.get( objectName );
        if( !existing ) {
            LOGGER.debug( "addMetric: adding new MBean ${objectName}" );
            mets.put( objectName, metric );
        }
        else {
            LOGGER.warn( "addMetric: attempting to re-register the same MBean ${objectName}, ignoring", new Exception() );
        }
    }
        
    void initialize() {
        callInClassLoader( context.loader ) {
            LOGGER.debug( "initialize: name=${name}");
            context.runScript( this );    
            try {
                tests.each{ Test test ->
                    test.initialize();
                } 
                registerMBeans();
            }
            catch( Exception ex ) {
                LOGGER.error( "Failed initializing, attempting cleanup...", ex );
                try {
                    unregisterMBeans();
                }
                catch( Exception ex2 ) {
                    LOGGER.error( "Error unregistering MBeans after failed init", ex );
                }
                try {
                    destroy?.call();
                }
                catch( Exception ex2 ) {
                    LOGGER.error( "Error trying to cleanup from failed init", ex );
                }
                throw ex;
            }
        }
    }

    void addTest( String name, def params ) {
        tests.add( new Test(name,params) );
    }
   
    void setDestroyCallback( Closure destroy ) {
        this.destroy = destroy;
    } 
     
    private void registerMBeans() {
        scriptMetrics.values().each{ ScriptMetric metric ->
            metric.register();
        }
        tests.each{ Test test ->
            test.registerMBeans();
        }
    }    
    
    private void unregisterMBeans() {
        scriptMetrics.values().each{ ScriptMetric metric ->
            metric.unregister();
        }
        tests.each{ Test test ->
            test.unregisterMBeans();
        }
    } 

    synchronized int getNumRunners() {
        return runners.size();
    }    
    
    synchronized void start( int threadCount, List arguments ) {
        if( runners.size() > threadCount ) {
            int sz = runners.size();
            LOGGER.debug( "start: increasing threads from ${sz} to ${threadCount}");
            for( int i=threadCount ; i < sz ; i++ ) {
                runners[i].stop();
            }
            while( runners.size() > threadCount ) {
                Runner runner = runners.remove( runners.size() - 1);
                runner.join();
            }
        }
        else if( runners.size() < threadCount ) {
            LOGGER.debug( "start: decreasing threads from ${runners.size()} to ${threadCount}");
            while( runners.size() < threadCount ) {
                Runner runner = new Runner(arguments);
                runners.add( runner );
                runner.start();
            }
        }
        else {
            LOGGER.debug( "start: no change from ${runners.size()} threads");
        }
    } 
   
    synchronized void stop() {
        LOGGER.debug( "stop: stopping ${runners.size()} threads")
        try {
	        runners.each{ Runner runner ->
	            runner.stop();
	        }
	        runners.each{ Runner runner ->
	            runner.join();
	        }
        }
        finally {
		    runners.clear();
        }
    }
    
    synchronized void shutdown() {
        LOGGER.debug( "shutdown: shutting down script")
        try {
            stop();
        }
        finally {
            try {
               unregisterMBeans(); 
            }
            catch( Exception ex ) {
                LOGGER.error( "Error unregistering MBeans", ex );
            }
            
            try {
                callInClassLoader( context.loader, destroy );
            }
            catch( Exception ex ) {
                LOGGER.error( "Error invoking destroy", ex );
            }
        }
    }

    /**
     * A runner stopping normally will have its running flag set to false and exit. One that
     * fails without the scripts knowledge or consent must be removed from the list manually.
     * This should only be the case in a catastrphic error.
     * @param runner
     */
    synchronized void removeRunnerExplicity( Runner runner ) {
        LOGGER.info( "removeRunnerExplicity: load runner removed manually: ${runner}");
        runners.remove( runner );
    } 
    
    String toString() {
       return "[name=${name}, threads=${runners.size()}]"; 
    } 
}

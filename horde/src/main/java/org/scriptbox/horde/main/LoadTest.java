package org.scriptbox.horde.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.scriptbox.box.container.BoxScript;
import org.scriptbox.horde.metrics.AvgTransactionTime;
import org.scriptbox.horde.metrics.FailureCount;
import org.scriptbox.horde.metrics.MaxTransactionTime;
import org.scriptbox.horde.metrics.MinTransactionTime;
import org.scriptbox.horde.metrics.TestMetric;
import org.scriptbox.horde.metrics.TransactionCount;
import org.scriptbox.horde.metrics.TransactionsPerSecond;
import org.scriptbox.util.common.collection.CollectionUtil;

public class LoadTest {

	BoxScript script;
    String name;
    Runnable init;
    Runnable run;
    Runnable pre;
    Runnable post;
    
    Map<ObjectName,TestMetric> preMetrics = new HashMap<ObjectName,TestMetric>();
    Map<ObjectName,TestMetric> metrics = new HashMap<ObjectName,TestMetric>();
    Map<ObjectName,TestMetric> postMetrics = new HashMap<ObjectName,TestMetric>();

    public LoadTest( String name ) {
        this.name = name;
    }
    
    void initialize() {
    	
        if( run == null ) {  
            throw new Exception( "No run block defined for test");
        }
        if( init != null ) {
        	init.run();
        }
        if( CollectionUtil.isEmpty(metrics) && 
        	CollectionUtil.isEmpty(preMetrics) && 
        	CollectionUtil.isEmpty(postMetrics) )  {
            if( pre != null ) {
                addPreMetric( new AvgTransactionTime("pre") );
            }
	        addRunMetric( new TransactionsPerSecond() );
	        addRunMetric( new TransactionCount() );
	        addRunMetric( new MinTransactionTime() );
	        addRunMetric( new MaxTransactionTime() );
	        addRunMetric( new AvgTransactionTime() );
	        addRunMetric( new FailureCount() );
            if( post != null ) {
                addPostMetric( new AvgTransactionTime("post") );
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
        metric.init( script, this );
        addMetric( mets, metric );
    }
   
    Object getTestAttribute( String name ) {
       return attributes.get().get( name ); 
    } 
    void setTestAttribute( String name, Object value ) {
        attributes.get().put( name, value );
    }

    void callAndCollectMetrics( List arguments, Map<ObjectName,TestMetric> met, Runnable closure ) {
        if( closure != null ) {
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

package org.scriptbox.metrics.beans;

import java.util.concurrent.atomic.AtomicInteger;

public class FailureCount extends RecordableMetric {

    private AtomicInteger failures = new AtomicInteger();
     
    public FailureCount() {
    }
 
    public String getName() {
        return "failures";
    } 
    
    public String getDescription() {
    	return "Failure Count";
    }
    
    public void record( boolean success, long millis ) {
        if( !success ) {
	        failures.addAndGet( 1 );    
        }
    }
    
    public float getValue() {
        return failures.getAndSet( 0 );    
    }
}

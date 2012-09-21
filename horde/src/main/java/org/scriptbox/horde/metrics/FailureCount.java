package org.scriptbox.horde.metrics;

import java.util.concurrent.atomic.AtomicInteger;

public class FailureCount extends ActionMetric {

    private AtomicInteger failures = new AtomicInteger();
     
    public FailureCount() {
    }
 
    public String getName() {
        return "failures";
    } 
    
    public void record( boolean success, long millis ) {
        if( !success ) {
	        failures.addAndGet( 1 );    
        }
    }
    
    public int getValue() {
        return failures.getAndSet( 0 );    
    }
}

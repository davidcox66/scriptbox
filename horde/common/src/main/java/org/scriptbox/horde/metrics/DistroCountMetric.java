package org.scriptbox.horde.metrics;

import java.util.concurrent.atomic.AtomicInteger;


public class DistroCountMetric extends AbstractDistroMetric {

    private AtomicInteger count = new AtomicInteger();
    
    public DistroCountMetric( String name, long low, long high ) {
    	super( name, low, high );
    }
    
    public void record( boolean success, long millis ) {
    	if( millis >= low && (high < 0 || millis <= high) ) {
    		count.addAndGet(1);
    	}
    }
    
    public float getValue() {
    	return count.getAndSet(0);
    }
}

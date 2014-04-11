package org.scriptbox.metrics.beans.distro;

import java.util.concurrent.atomic.AtomicInteger;


public class DistroCountMetric extends AbstractDistroMetric {

    private AtomicInteger count = new AtomicInteger();
    
    public DistroCountMetric( String name, long low, long high ) {
    	super( name, low, high );
    }
    
    public void record( boolean success, long millis ) {
    	if( isInRange(millis) ) {
    		count.addAndGet(1);
    	}
    }
    
    public float getValue() {
    	return count.getAndSet(0);
    }
}

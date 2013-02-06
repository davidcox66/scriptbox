package org.scriptbox.horde.metrics;

import java.util.concurrent.atomic.AtomicInteger;


public class DistroMetric extends ActionMetric {

    private String name;
    private String fullName;
    private long low;
    private long high;
    private AtomicInteger count = new AtomicInteger();
    
    public DistroMetric( String name, long low, long high ) {
    	this.name = name;
    	if( high > 0 ) {
	        this.fullName = String.format( "%s_%05d_%05d", name, low, high );
    	}
    	else {
	        this.fullName = String.format( "%s_%05d_XXXXX", name, low );
    	}
        this.low = low;
        this.high = high;
    }
    
    public String getName() {
        return fullName;
    } 
    
    public String getDescription() {
    	return name + " distribution " + low + "-" + (high > 0 ? ""+high : "beyond" );
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

package org.scriptbox.horde.metrics;


public class DistroPercentMetric extends AbstractDistroMetric {

    private int count;
    private int total;
    
    public DistroPercentMetric( String name, long low, long high ) {
    	super( name, low, high );
    }
    
    synchronized public void record( boolean success, long millis ) {
    	if( millis >= low && (high < 0 || millis <= high) ) {
    		total++;
    	}
    	count++;
    }
    
    synchronized public float getValue() {
    	float ret = count > 0 ? ((float)total) / count : 0;
    	total = count = 0;
    	return ret;
    }
}

package org.scriptbox.horde.metrics.probe;

import org.scriptbox.horde.metrics.ActionMetric;


public class TimingProbeAverage extends ActionMetric {

    private String name;
    private String description;
    private int transactionCount;
    private long totalMillis;
     
    public TimingProbeAverage( String name, String description ) {
        this.name = name;
        this.description = description;
    }
    
    public String getName() {
        return name;
    } 
    
    public String getDescription() {
    	return description;
    }
    
    public void record( boolean success, long millis ) {
        transactionCount++;
        totalMillis += millis;
    }
    
    synchronized public float getValue() {
        if( transactionCount > 0 ) {
	        float ret = totalMillis / transactionCount;
	        transactionCount = 0;
	        totalMillis = 0;
	        
	        return ret;
        }
        return 0;
    }
}

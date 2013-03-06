package org.scriptbox.horde.metrics;


public class MaxTransactionTime extends RecordableMetric {

    private int max = Integer.MIN_VALUE;
     
    public MaxTransactionTime() {
    }
 
    public String getName() {
        return "max";
    } 
    
    public String getDescription() {
    	return "Maximum Transaction Time";
    }
    
    synchronized public void record( boolean success, long millis ) {
        max = (int)Math.max( max, millis );
    }
    
    synchronized public float getValue() {
        int val = max;
        max = Integer.MIN_VALUE;
        return val != Integer.MIN_VALUE ? val : 0;
    }
}

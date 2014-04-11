package org.scriptbox.metrics.beans;

public class MinTransactionTime extends RecordableMetric {

    private int min = Integer.MAX_VALUE;
     
    public MinTransactionTime() {
    }
 
    public String getName() {
        return "min";
    } 
    
    public String getDescription() {
    	return "Minimum Transaction Time";
    }
    synchronized public void record( boolean success, long millis ) {
        min = (int)Math.min( min, millis );
    }
    
    synchronized public float getValue() {
        int val = min;
        min = Integer.MAX_VALUE;
        return val != Integer.MAX_VALUE ? val : 0;
    }
}

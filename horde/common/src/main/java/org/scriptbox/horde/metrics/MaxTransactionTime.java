package org.scriptbox.horde.metrics;


public class MaxTransactionTime extends ActionMetric {

    private int max = Integer.MIN_VALUE;
     
    public MaxTransactionTime() {
    }
 
    public String getName() {
        return "max";
    } 
    
    synchronized public void record( boolean success, long millis ) {
        max = (int)Math.max( max, millis );
    }
    
    synchronized public int getValue() {
        int val = max;
        max = Integer.MIN_VALUE;
        return val != Integer.MIN_VALUE ? val : 0;
    }
}

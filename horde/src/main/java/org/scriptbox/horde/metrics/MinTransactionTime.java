package org.scriptbox.horde.metrics;

public class MinTransactionTime extends TestMetric {

    private int min = Integer.MAX_VALUE;
     
    MinTransactionTime() {
    }
 
    public String getName() {
        return "min";
    } 
    
    synchronized public void record( boolean success, long millis ) {
        min = (int)Math.min( min, millis );
    }
    
    synchronized public int getValue() {
        int val = min;
        min = Integer.MAX_VALUE;
        return val != Integer.MAX_VALUE ? val : 0;
    }
}

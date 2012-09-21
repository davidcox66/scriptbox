package org.scriptbox.horde.metrics;


public class TransactionsPerSecond extends ActionMetric {

    private long lastChecked = Long.MIN_VALUE;
    private int transactionCount;
     
    public TransactionsPerSecond() {
    }
 
    public String getName() {
        return "tps";
    } 
    
    synchronized public void record( boolean success, long millis ) {
        if( lastChecked == Long.MIN_VALUE ) {
           lastChecked = System.currentTimeMillis(); 
        }
        transactionCount++;
    }
    
    synchronized public int getValue() {
        long now = System.currentTimeMillis();
        float divisor = (now - lastChecked) / 1000;
        try {
            return divisor > 0 ? (int)(transactionCount / divisor) : 0;
        }
        finally {
	        transactionCount = 0;
	        lastChecked = now; 
        }
    }
}

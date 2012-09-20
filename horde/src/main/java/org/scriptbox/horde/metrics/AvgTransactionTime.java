package org.scriptbox.horde.metrics;


public class AvgTransactionTime extends TestMetric {

    private String name;
    private int transactionCount;
    private long totalMillis;
     
    public AvgTransactionTime() {
        this( "avg" );
    }
    
    public AvgTransactionTime( String name ) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    } 
    
    synchronized public void record( boolean success, long millis ) {
        transactionCount++;
        totalMillis += millis;
    }
    
    synchronized public int getValue() {
        if( transactionCount > 0 ) {
	        int ret = (int)(totalMillis / transactionCount);
	        transactionCount = 0;
	        totalMillis = 0;
	        
	        return ret;
        }
        return 0;
    }
}

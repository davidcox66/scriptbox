package org.scriptbox.horde.metrics;


public class AvgTransactionTime extends ActionMetric {

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
    
    public String getDescription() {
    	return "Average Transaction Time";
    }
    
    synchronized public void record( boolean success, long millis ) {
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

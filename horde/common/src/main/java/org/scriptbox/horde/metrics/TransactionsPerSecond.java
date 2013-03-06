package org.scriptbox.horde.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionsPerSecond extends RecordableMetric {

	private static final Logger LOGGER = LoggerFactory.getLogger( TransactionsPerSecond.class );
	
    private long lastChecked = 0;
    private int transactionCount;
     
    public TransactionsPerSecond() {
    }
 
    public String getName() {
        return "tps";
    } 
    
    public String getDescription() {
    	return "Transactions Per Second";
    }
    
    synchronized public void record( boolean success, long millis ) {
        if( lastChecked == 0 ) {
           lastChecked = System.currentTimeMillis(); 
        }
        transactionCount++;
    }
    
    synchronized public float getValue() {
        long now = System.currentTimeMillis();
        try {
        	if( lastChecked > 0 ) {
	        	double divisor = (now - lastChecked) / 1000;
	        	return divisor > 0 ? (float)(transactionCount / divisor) : 0.0F;
        	}
        }
        catch( Exception ex ) {
        	LOGGER.error( "getValue: error computing transactions per second", ex );
        }
        finally {
	        transactionCount = 0;
	        lastChecked = now; 
        }
		return 0;
    }
}

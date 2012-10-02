package org.scriptbox.horde.metrics;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionCount extends ActionMetric {

    private AtomicInteger transactionCount = new AtomicInteger();
     
    public TransactionCount() {
    }
 
    public String getName() {
        return "count";
    } 
    
    public String getDescription() {
    	return "Transaction Count";
    }
    
    synchronized public void record( boolean success, long millis ) {
        transactionCount.addAndGet( 1 );
    }
    
    synchronized public int getValue() {
        return transactionCount.getAndSet( 0 );
    }
}

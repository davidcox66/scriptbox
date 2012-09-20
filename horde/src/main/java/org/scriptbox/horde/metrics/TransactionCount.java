package com.ihg.atp.crs.loadgen.metrics

import java.util.concurrent.atomic.AtomicInteger

import com.ihg.atp.crs.loadgen.main.LoadScript

class TransactionCount extends TestMetric {

    private AtomicInteger transactionCount = new AtomicInteger();
     
    TransactionCount() {
    }
 
    String getName() {
        return "count";
    } 
    
    synchronized void record( boolean success, long millis ) {
        transactionCount.addAndGet( 1 );
    }
    
    synchronized int getValue() {
        return transactionCount.getAndSet( 0 );
    }
}

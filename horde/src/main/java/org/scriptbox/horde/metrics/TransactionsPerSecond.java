package com.ihg.atp.crs.loadgen.metrics

import java.util.concurrent.atomic.AtomicInteger

import com.ihg.atp.crs.loadgen.main.LoadScript

class TransactionsPerSecond extends TestMetric {

    private long lastChecked = Long.MIN_VALUE;
    private int transactionCount;
     
    TransactionsPerSecond() {
    }
 
    String getName() {
        return "tps";
    } 
    
    synchronized void record( boolean success, long millis ) {
        if( lastChecked == Long.MIN_VALUE ) {
           lastChecked = System.currentTimeMillis(); 
        }
        transactionCount++;
    }
    
    synchronized int getValue() {
        long now = System.currentTimeMillis();
        float divisor = (now - lastChecked) / 1000;
        try {
            return divisor > 0 ? transactionCount / divisor : 0;
        }
        finally {
	        transactionCount = 0;
	        lastChecked = now; 
        }
    }
}

package com.ihg.atp.crs.loadgen.metrics

import java.util.concurrent.atomic.AtomicInteger

import com.ihg.atp.crs.loadgen.main.LoadScript

class MaxTransactionTime extends TestMetric {

    private int max = Integer.MIN_VALUE;
     
    MaxTransactionTime() {
    }
 
    String getName() {
        return "max";
    } 
    
    synchronized void record( boolean success, long millis ) {
        max = Math.max( max, millis );
    }
    
    synchronized int getValue() {
        int val = max;
        max = Integer.MIN_VALUE;
        return val != Integer.MIN_VALUE ? val : 0;
    }
}

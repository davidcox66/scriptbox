package com.ihg.atp.crs.loadgen.metrics

import java.util.concurrent.atomic.AtomicInteger

import com.ihg.atp.crs.loadgen.main.LoadScript

class MinTransactionTime extends TestMetric {

    private int min = Integer.MAX_VALUE;
     
    MinTransactionTime() {
    }
 
    String getName() {
        return "min";
    } 
    
    synchronized void record( boolean success, long millis ) {
        min = Math.min( min, millis );
    }
    
    synchronized int getValue() {
        int val = min;
        min = Integer.MAX_VALUE;
        return val != Integer.MAX_VALUE ? val : 0;
    }
}

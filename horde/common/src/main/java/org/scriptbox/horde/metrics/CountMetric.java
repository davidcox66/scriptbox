package org.scriptbox.horde.metrics;

import java.util.concurrent.atomic.AtomicInteger;

public class CountMetric extends ActionMetric {

	private String name;
	private String description;
    private AtomicInteger total = new AtomicInteger();

    public CountMetric( String name, String description ) {
    	this.name = name;
    	this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void record( boolean success, long millis ) {
    }

    public void increment() {
        total.addAndGet(1);
    }

    public float getValue() {
        return total.getAndSet(0);
    }
}

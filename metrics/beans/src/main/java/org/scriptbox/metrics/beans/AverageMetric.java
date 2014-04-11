package org.scriptbox.metrics.beans;


public class AverageMetric extends RecordableMetric {

	private String name;
	private String description;
	private int count;
    private int total;

    public AverageMetric( String name, String description ) {
    	this.name = name;
    	this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    synchronized public void record( boolean success, long millis ) {
    	count++;
    }

    public void increment() {
    	add( 1 );
    }
    synchronized public void add( int value ) {
    	total += value;
    }

    synchronized public float getValue() {
    	float ret = count > 0 ? ((float)total / count) : 0.0F;
    	count = 0;
    	total = 0;
        return ret;
    }
}

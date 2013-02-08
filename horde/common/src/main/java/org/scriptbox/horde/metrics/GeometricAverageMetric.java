package org.scriptbox.horde.metrics;


public class GeometricAverageMetric extends ActionMetric {

	private String name;
	private String description;
	private int count;
    private int total;
    private float value;
    private float weight;
    
    public GeometricAverageMetric( String name, String description, float weight ) {
    	if( weight <= 0 || weight >= 1.0) {
    		throw new IllegalArgumentException( "Weight must be between > 0 and < 1.0");
    	}
    	this.name = name;
    	this.description = description;
    	this.weight = weight;
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

    public float getCurrentValue() {
    	return value;
    }

    synchronized public float getComputedValue() {
    	float curr = ((float)total) / count;
    	return (curr * weight) + (value * (1.0F - weight));
    }
    
    public int getTotal() {
    	return total;
    }
    
    public int getCount() {
    	return count;
    }
    
    synchronized public float getValue() {
    	if( count > 0 ) {
	    	value = getComputedValue();
	    	count = 0;
	    	total = 0;
    	}
        return value;
    }
    
    public String toString() {
    	return "GeometricAverageMetric{ name=" + name + ", total=" + total + ", count=" + count + ", value=" + getComputedValue() + " }";
    }
}

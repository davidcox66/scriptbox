package org.scriptbox.metrics.beans;



public class DeviationMetric extends RecordableMetric {

	private String name;
	private String description;
	private int count;
    private double A;
    private double Q;

    public DeviationMetric() {
    	this( "deviation", "Standard Deviation" );
    }
    
    public DeviationMetric( String name, String description ) {
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
      	
    	final long xk = millis;
      	final double oldA = A;
      	A = A + (xk - A) / count;
      	Q = Q + (xk - oldA) * (xk - A);
    }

    synchronized public float getValue() {
    	float ret = (float)Math.sqrt( Q / count );
    	count = 0;
    	A = Q = 0;
    	return ret;
    }
    
    public String toString() {
    	return "DeviationMetric{ name=" + name + ", description=" + description + " }";
    }
}

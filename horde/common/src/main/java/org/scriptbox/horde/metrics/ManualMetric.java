package org.scriptbox.horde.metrics;


public class ManualMetric extends AbstractMetric {

	private String name;
	private String description;
    private float value;

    public ManualMetric( String name, String description ) {
    	this.name = name;
    	this.description = description;
    }
    
    public ManualMetric( String name, String description, float value ) {
    	this.name = name;
    	this.description = description;
    	this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setValue( float value ) {
    	this.value = value;
    }

    public float getValue() {
        return value;
    }
}

package org.scriptbox.metrics.model;

public class Metric {

	private long millis;
	private float value;
	
	public Metric( long millis, float value ) {
		this.millis = millis;
		this.value = value;
	}

	public long getMillis() {
		return millis;
	}

	public float getValue() {
		return value;
	}
	
	public boolean isBetween( long start, long end ) {
		return start <= millis && millis <= end;
	}
	
	public String toString() {
		return "Metric{ millis=" + millis + ", value=" + value + " }";
	}
}

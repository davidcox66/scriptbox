package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.Date;

public class Metric implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long millis;
	private float value;
	
	public Metric() {
	}
	
	public Metric( long millis, float value ) {
		this.millis = millis;
		this.value = value;
	}

	public Date getDate() {
		return new Date( millis );
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
		return "Metric{ millis=" + millis + ", date=" + getDate() + ", value=" + value + " }";
	}
}

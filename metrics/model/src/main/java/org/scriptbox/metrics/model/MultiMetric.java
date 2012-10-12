package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.Date;

public class MultiMetric implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long millis;
	private float[] values;
	
	public MultiMetric() {
	}
	
	public MultiMetric( long millis, float[] values ) {
		this.millis = millis;
		this.values = values;
	}

	public Date getDate() {
		return new Date( millis );
	}
	
	public long getMillis() {
		return millis;
	}

	public float[] getValues() {
		return values;
	}
	
	public boolean isBetween( long start, long end ) {
		return start <= millis && millis <= end;
	}
	
	public String toString() {
		return "MultiMetric{ millis=" + millis + ", values=" + values + " }";
	}
}

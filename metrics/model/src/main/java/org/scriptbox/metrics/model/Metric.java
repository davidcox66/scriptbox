package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.Date;

/**
 * An immutable object to hold a single collected metric value and the timestamp in which it occurred.
 * 
 * @author david
 */
public final class Metric implements Serializable, Timed {

	private static final long serialVersionUID = 1L;
	
	private long millis;
	private float value;
	
	public Metric() {
	}
	
	public Metric( long millis, float value ) {
		this.millis = millis;
		this.value = value;
	}

	public final Date getDate() {
		return new Date( millis );
	}
	
	public final long getMillis() {
		return millis;
	}

	public final float getValue() {
		return value;
	}

	public String toString() {
		return "Metric{ millis=" + millis + ", date=" + getDate() + ", value=" + value + " }";
	}
}

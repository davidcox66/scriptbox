package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class Metric implements Serializable, IsSerializable {

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
		return "Metric{ millis=" + millis + ", value=" + value + " }";
	}
}

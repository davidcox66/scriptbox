package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.Date;

/**
 * The GXT model for charting API model does not work with individual series of data as is generally 
 * represented in MetricRange and List<Metric> on the server. It uses a list of objects where each 
 * object has all of the data points for a specific moment in time. We go through a transformation
 * into this structure when showing multiple metrics on a single chart.
 *
 * @see MetricCollator.multi
 * 
 * @author david
 *
 */
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
	
	public String toString() {
		return "MultiMetric{ millis=" + millis + ", values=" + values + " }";
	}
}

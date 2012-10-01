package org.scriptbox.metrics.model;


public abstract class MetricSequence {

	public abstract void record( Metric metric );

	public abstract DateRange getDateRange();
	public abstract MetricRange getRange( long start, long end, int resolution );
	public abstract void deleteAll();
	
}

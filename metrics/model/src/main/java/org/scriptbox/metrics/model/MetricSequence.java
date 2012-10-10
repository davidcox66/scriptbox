package org.scriptbox.metrics.model;


public abstract class MetricSequence {

	public abstract void record( Metric metric );

	public abstract DateRange getFullDateRange();
	public abstract MetricRange<? extends Metric> getRange( long start, long end );
	public abstract void delete();
	
}

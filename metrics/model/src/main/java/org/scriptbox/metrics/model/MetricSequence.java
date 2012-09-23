package org.scriptbox.metrics.model;


public abstract class MetricSequence {

	public abstract void record( Metric metric );
	
	public abstract MetricRange getRange( long start, long end, int resolution );
	public abstract void deleteAllRange( long start, long end, int resolution );
	public abstract void deleteAllRangeResolutions( long start, long end );
	public abstract void deleteAll();
	
}

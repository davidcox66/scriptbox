package org.scriptbox.metrics.model;

import org.scriptbox.metrics.query.MetricQueryContext;
import org.scriptbox.metrics.query.MetricProvider;


public abstract class MetricSequence implements MetricProvider {

	public abstract void record( Metric metric );

	public abstract DateRange getFullDateRange();
	public abstract MetricRange getRange( long start, long end );
	public abstract void delete();
	
	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return getRange(ctx.getStart(),ctx.getEnd());
	}
}

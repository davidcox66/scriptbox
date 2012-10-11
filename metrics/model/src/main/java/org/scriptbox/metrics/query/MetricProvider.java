package org.scriptbox.metrics.query;

import org.scriptbox.metrics.model.MetricRange;

public interface MetricProvider {

	public String getName();
	public String getId( int seconds );
	public boolean isPersistent();
	public int getNearestResolutionSeconds( int seconds );
	public MetricRange getMetrics( MetricQueryContext ctx );
}

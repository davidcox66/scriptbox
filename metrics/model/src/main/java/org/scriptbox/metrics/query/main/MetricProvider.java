package org.scriptbox.metrics.query.main;

import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.MetricRange;

public interface MetricProvider {

	public String getName();
	public boolean isPersistent();
	public DateRange getFullDateRange();
	public MetricRange getMetrics( MetricQueryContext ctx );
}

package org.scriptbox.metrics.query;

import org.scriptbox.metrics.model.MetricRange;

public interface MetricValueProvider {

	public MetricRange getValues( MetricQueryContext ctx );
}

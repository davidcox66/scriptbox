package org.scriptbox.metrics.query.exp;

import org.scriptbox.metrics.query.main.MetricQueryContext;

public interface MetricQueryExp {

	public Object evaluate( MetricQueryContext ctx ) throws Exception;
}

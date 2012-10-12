package org.scriptbox.metrics.cassandra;

import org.scriptbox.metrics.query.main.MetricProvider;

public interface CassandraMetricProvider extends MetricProvider {

	public String getId( int seconds );
	public int getNearestResolutionSeconds( int seconds );
}

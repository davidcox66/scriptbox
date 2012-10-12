package org.scriptbox.metrics.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public interface MetricStore {

	public List<MetricTree> getAllMetricTrees();
	public MetricTree getMetricTree( String name );
	public MetricTree createMetricTree( String name, List<MetricResolution> resolutions );
	public Map<? extends MetricProvider,? extends MetricRange> getAllMetrics( final Collection<? extends MetricProvider> nodes, final MetricQueryContext ctx ); 
	
	public void begin();
	public void end();
}

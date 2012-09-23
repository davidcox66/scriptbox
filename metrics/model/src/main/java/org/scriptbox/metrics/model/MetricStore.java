package org.scriptbox.metrics.model;

import java.util.List;

public interface MetricStore {

	public List<MetricTree> getAllMetricTrees();
	public MetricTree getMetricTree( String name );
	public MetricTree createMetricTree( String name, List<MetricResolution> resolutions );
	
	public void begin();
	public void end();
}

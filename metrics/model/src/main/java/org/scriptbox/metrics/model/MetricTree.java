package org.scriptbox.metrics.model;

import java.util.List;

public abstract class MetricTree {

	protected String name;
	protected List<MetricResolution> resolutions;
	
	public MetricTree( String name, List<MetricResolution> resolutions ) {
		this.name = name;
		this.resolutions = resolutions;
	}

	public String getName() {
		return name;
	}

	public List<MetricResolution> getResolutions() {
		return resolutions;
	}
	
	public abstract MetricTreeNode getRoot(); 
	
	
}

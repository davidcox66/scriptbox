package org.scriptbox.metrics.model;

import java.util.List;

public abstract class MetricTree {

	protected String name;
	protected List<MetricResolution> resolutions;
	
	public MetricTree( String name, List<MetricResolution> resolutions ) {
		this.name = name;
		this.resolutions = resolutions;
	}

	public abstract MetricTreeNode getRoot(); 
	
	public String getName() {
		return name;
	}

	public List<MetricResolution> getResolutions() {
		return resolutions;
	}
	
	public MetricResolution getResolutionEx( int resolution ) {
		for( MetricResolution res : resolutions ) {
			if( res.getSeconds() == resolution ) {
				return res;
			}
		}
		throw new RuntimeException( "Invalid resolution '" + resolution + "' for tree '" + name + "'" );
	}
	
}

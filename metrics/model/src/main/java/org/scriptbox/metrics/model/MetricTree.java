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

	public MetricResolution getNearestResolution( int seconds ) {
		MetricResolution best = null;
		for( MetricResolution res : resolutions ) {
			if( res.getSeconds() < seconds ) {
				if( best == null || best.getSeconds() < res.getSeconds() ) {
					best = res;
				}
			}
		}
		return best != null ? best : getFinestResolution();
	}
	
	public MetricResolution getFinestResolution() {
		int min = Integer.MAX_VALUE;
		MetricResolution ret = null;
		for( MetricResolution res : resolutions ) {
			if( res.getSeconds() < min ) {
				ret = res;
				min = res.getSeconds();
			}
		}
		return ret;
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

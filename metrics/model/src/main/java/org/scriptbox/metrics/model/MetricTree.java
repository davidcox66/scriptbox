package org.scriptbox.metrics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public abstract class MetricTree {

	protected String name;
	protected List<MetricResolution> resolutions;
	
	public MetricTree( String name, List<MetricResolution> resolutions ) {
		if( StringUtils.isEmpty(name) ) {
			throw new IllegalArgumentException( "Name cannot be null");
		}
		if( resolutions == null || resolutions.size() == 0 ) {
			throw new IllegalArgumentException( "Resolutions cannot be empty");
		}
		
		this.name = name;
		List<MetricResolution> res = new ArrayList<MetricResolution>(resolutions);
		Collections.sort( res );
		this.resolutions = Collections.unmodifiableList( res );
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
		return resolutions.get(0);
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

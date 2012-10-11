package org.scriptbox.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
	public abstract void delete();
	public abstract Map<MetricTreeNode,MetricRange> getRanges( Collection<MetricTreeNode> nodes, long start, long end );
	
	public String getName() {
		return name;
	}

	public void visitNodes( MetricTreeVisitor visitor ) {
		MetricTreeNode root = getRoot();
		visitNodeImpl( root, visitor );
	}
	
	private void visitNodeImpl( MetricTreeNode node, MetricTreeVisitor visitor ) {
		if( visitor.visit(node) ) {
			for( MetricTreeNode child : node.getChildren().values() ) {
				visitNodeImpl( child, visitor );
			}
		}
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

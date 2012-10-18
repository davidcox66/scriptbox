package org.scriptbox.metrics.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This is the highest organization for a group of metrics. This may be used to segregate data for
 * different load tests, environments, or whatever purpose is necessary.
 * 
 * @author david
 *
 */
public abstract class MetricTree {

	protected String name;
	
	/**
	 * The tree retains a list of objects containing various increments of time (seconds) in which metrics
	 * will be collected.
	 * 
	 * @see MetricResolution
	 */
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

	/**
	 * Gets the topmost node in the tree
	 */
	public abstract MetricTreeNode getRoot(); 
	
	/**
	 * Deletes the hierarchy of tree nodes and all associated metrics
	 */
	public abstract void delete();
	
	public String getName() {
		return name;
	}

	/**
	 * Clients may ask for metrics of potentially any number of seconds. This attempts to
	 * find the nearest this as fine or finer than the supplied number of seconds. This
	 * method is used extensively by subordinate objects within the tree.
	 * @param seconds
	 * @return
	 */
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
	
	/**
	 * Gets the resolution with the shortest (finest) number of seconds
	 */
	public MetricResolution getFinestResolution() {
		return resolutions.get(0);
	}
	
	public List<MetricResolution> getResolutions() {
		return resolutions;
	}
	
	/**
	 * Goes through each node in the tree, passing each node to the supplied visitor
	 */
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
	
}

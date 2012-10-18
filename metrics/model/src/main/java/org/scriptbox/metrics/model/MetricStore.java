package org.scriptbox.metrics.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueryContext;

/**
 * Top-most interface for interacting with a data store for metrics. Though there may be many association and
 * interactions of subordinate objects in the store, this interface and those of its subordinates are kept 
 * to the minimal details, allowing for the most flexibility in implementation.
 * 
 * @author david
 *
 */
public interface MetricStore {

	/**
	 * All metrics are organized into hierarchical structures. This segregates metrics for various purposes, 
	 * different load tests, different environments, etc.
	 */
	public List<MetricTree> getAllMetricTrees();
	
	public MetricTree getMetricTree( String name );
	
	/**
	 * 
	 * @param name a user-defined name for the tree
	 * @param resolutions a list of objects containing the seconds in which metrics will be collected 
	 * @see MetricResolution
	 * @return the newly created tree
	 */
	public MetricTree createMetricTree( String name, List<MetricResolution> resolutions );
	
	/**
	 * This is used by the metric query functionality to do batch queries of metric values based upon
	 * a given date range.
	 *  
	 * @param nodes
	 * @param ctx
	 * @return
	 */
	public Map<? extends MetricProvider,? extends MetricRange> getAllMetrics( final Collection<? extends MetricProvider> nodes, final MetricQueryContext ctx ); 
	
	/**
	 * All interactions with the metric store should be within the context of a begin() and end() calls. This allows 
	 * the store to make various optimizations such as caching metrics that were previously queried. This
	 * can happen regularly as reports often show different aspects of underlying data (min, max, avg, etc).
	 */
	public void begin();
	
	/**
	 * Indicate that no more immediate queries will be issued as part of the current client interaction. The
	 * store is free to clean up any cached data if it chooses.
	 */
	public void end();
}

package org.scriptbox.metrics.model;

import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueryContext;

/**
 * An abstraction for the full time range of a collected metric, irrespective of time range
 * or resolution.
 *  
 * @author david
 */
public abstract class MetricSequence implements MetricProvider {

	/**
	 * Saves the given metric to the data store. This should honor the resolutions specified on the tree,
	 * saving the value in the appropriate increments of time. 
	 * @param metric
	 */
	public abstract void record( Metric metric );

	/**
	 * Gets the times of the first and last metrics which have been saved
	 */
	public abstract DateRange getFullDateRange();
	
	/**
	 * Gets a range abstraction for the specified time range of metrics
	 * @param start
	 * @param end
	 * @return
	 */
	public abstract MetricRange getRange( long start, long end );
	
	/**
	 * Deletes all collected data for this metrics and all its resolutions
	 */
	public abstract void delete();
	
	/**
	 * Needed by the query APIs to fetch a set of metrics. The query APIs work in terms of the more general
	 * MetricProvider interface.
	 */
	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return getRange(ctx.getStart(),ctx.getEnd());
	}
}

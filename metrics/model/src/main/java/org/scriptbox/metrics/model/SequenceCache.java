package org.scriptbox.metrics.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The act of retrieving the first and last metric times is a common enough occurrence that caching these
 * values saves us some trips to the data store. Data is stored in thread locals whose lifetime is the
 * duration of a client request. This works similarly to the MetricCache
 * 
 * @see MetricCache
 * 
 * @author david
 */
public class SequenceCache {

	private static final ThreadLocal<Map<MetricTreeNode,DateRange>> cache = new ThreadLocal<Map<MetricTreeNode,DateRange>>();
	
	/**
	 * Called at the beginning of an interaction with the metric store to create a fresh cache
	 * for newly created values.
	 */
	public static void begin() {
		cache.set( new HashMap<MetricTreeNode,DateRange>() );
	}
	
	/**
	 * Discards all cached values at the end of client interaction.
	 */
	public static void end() {
		cache.remove();
	}
	
	public static void put( MetricTreeNode node, DateRange range ) {
		Map<MetricTreeNode,DateRange> map = cache.get();
		if( map != null ) {
			map.put( node, range );
		}
	}
	
	public static DateRange get( MetricTreeNode sequence ) {
		Map<MetricTreeNode,DateRange> map = cache.get();
		if( map != null ) {
			map.get( sequence );
		}
		return null;
	}
}

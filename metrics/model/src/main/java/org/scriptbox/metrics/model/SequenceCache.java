package org.scriptbox.metrics.model;

import java.util.HashMap;
import java.util.Map;

public class SequenceCache {

	private static final ThreadLocal<Map<MetricTreeNode,DateRange>> cache = new ThreadLocal<Map<MetricTreeNode,DateRange>>();
	
	public static void begin() {
		cache.set( new HashMap<MetricTreeNode,DateRange>() );
	}
	
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

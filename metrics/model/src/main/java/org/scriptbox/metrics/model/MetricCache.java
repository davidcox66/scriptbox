package org.scriptbox.metrics.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used to retain queried metrics for a short duration - typically the duration of a single client request.
 * Queries of this kind may happen regularly as reports often show different representation (min, max, avg, etc)
 * of the same underlying data. Caching saves repeated round trips to the underlying data store. 
 * 
 * Also, implementations of a metric store and its related classes may use this as their means of retaining
 * collected metrics instead of holding them as properties of their own data structures. This makes sense because
 * the need to retain the values themselves may be short-lived though the other parts of the
 * metric hierarchy are more long-lived.
 * 
 * @author david
 */
public class MetricCache {

	private static ThreadLocal<Map<CacheKey,List<Metric>>> cache = new ThreadLocal<Map<CacheKey,List<Metric>>>();

	static class CacheKey
	{
		MetricRange range;
		int resolution;
		
		CacheKey( MetricRange range, int resolution ) {
			this.range = range;
			this.resolution = resolution;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((range == null) ? 0 : range.hashCode());
			result = prime * result + resolution;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (range == null) {
				if (other.range != null)
					return false;
			} else if (!range.equals(other.range))
				return false;
			if (resolution != other.resolution)
				return false;
			return true;
		}
		
	}
	
	/**
	 * Called at the beginning of an interaction with the metric store to create a fresh cache
	 * for newly created values.
	 */
	public static void begin() {
		cache.set( new HashMap<CacheKey,List<Metric>>() );
	}
	
	/**
	 * Discards all cached values at the end of client interaction.
	 */
	public static void end() {
		cache.remove();
	}

	public static List<Metric> get( MetricRange range, int seconds ) {
		CacheKey key = new CacheKey( range, seconds );
		Map<CacheKey,List<Metric>> lcache = cache.get();
		if( lcache != null ) {
			List<Metric> cached = lcache.get( key );
			if( cached != null ) {
				return cached;
			}
		}
		return null;
	}

	public static void put( MetricRange range, int seconds, List<Metric> values ) {
		Map<CacheKey,List<Metric>> lcache = cache.get();
		if( lcache != null ) {
			CacheKey key = new CacheKey( range, seconds );
			lcache.put( key, values );
		}
	}
	
	public static boolean contains( MetricRange range, int seconds ) {
		Map<CacheKey,List<Metric>> lcache = cache.get();
		if( lcache != null ) {
			CacheKey key = new CacheKey( range, seconds );
			return lcache.containsKey(key);
		}
		return false;
	}
}

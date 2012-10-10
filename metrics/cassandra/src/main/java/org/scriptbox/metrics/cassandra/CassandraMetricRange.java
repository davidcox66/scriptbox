package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;

public class CassandraMetricRange extends MetricRange {

	private static ThreadLocal<Map<CacheKey,List<Metric>>> cache = new ThreadLocal<Map<CacheKey,List<Metric>>>();
	
	private CassandraMetricSequence sequence;

	static class CacheKey
	{
		CassandraMetricRange range;
		int resolution;
		
		CacheKey( CassandraMetricRange range, int resolution ) {
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
	
	static void begin() {
		cache.set( new HashMap<CacheKey,List<Metric>>() );
	}
	static void end() {
		cache.remove();
	}
	
	public CassandraMetricRange( CassandraMetricSequence sequence, long start, long end ) {
		super( start, end );
		this.sequence = sequence;
	}
	
	public List<Metric> getMetrics( int resolution ) {
		int size = (int)((getEnd() - getStart()) / (resolution * 1000));
		List<Metric> ret = new ArrayList<Metric>( size );
		Iterator<Metric> iter = getIterator( resolution );
		while( iter.hasNext() ) {
			ret.add( iter.next() );
		}
		return ret;
	}
	
	public Iterator<Metric> getIterator( final int resolution ) {
		final List<Metric> metrics = fetchMetrics( resolution );
		return new Iterator<Metric>() {
			long cstart = getStart();
			Metric last = null;
			Metric pending = null;
			Iterator<Metric> iter = metrics.iterator();
			public boolean hasNext() {
				return iter.hasNext() || pending != null;
			}
			public Metric next() {
				Metric ret = null;
				long cend = cstart + resolution;
				if( last == null ) {
					pending = last = iter.next();
				}
				if( pending == null ) {
					pending = iter.next();
				}
				if( pending.isBetween(cstart, cend) ) {
					ret = last = pending;
					pending = null;
				}
				else {
					ret = new Metric(cstart, last.getValue() );
				}
				cstart += ret.getMillis();
				return ret;
			}
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported");
			}
		};
	}
	
	private List<Metric> fetchMetrics( int seconds ) {
		CacheKey key = new CacheKey( this, seconds );
		Map<CacheKey,List<Metric>> lcache = cache.get();
		if( lcache != null ) {
			List<Metric> cached = lcache.get( key );
			if( cached != null ) {
				return cached;
			}
		}
		
		List<Metric> metrics = queryMetrics( seconds );
		if( lcache != null ) {
			lcache.put( key, metrics );
		}
		return metrics;
	}
	
	protected List<Metric> queryMetrics( int seconds ) {
		MetricResolution resolution = sequence.node.tree.getNearestResolution( seconds );
		String compositeId = sequence.node.getId() + "," + resolution.getSeconds();
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(getStart(), getEnd(), false, Integer.MAX_VALUE );
		ColumnFamilyResult<String,Long> result = sequence.node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		
		Collection<Long> times = result.getColumnNames();
		List<Metric> metrics = new ArrayList<Metric>( times.size() + 2 ); // 2 for the potential of adding an earlier and later value
		Iterator<Long> iter = times.iterator();
	
		Long millis = null;
		Metric metric = null;
		if( iter.hasNext() ) {
			millis = iter.next();
			metric = new Metric( millis, result.getFloat(millis) );
			// If first metric is not near the beginning, try to find last previous value
			if( millis >= (getStart() + resolution.getSeconds()*1000) ) {
				Metric before = findRangeExteriorMetric(compositeId, getStart(), null, true );
				metrics.add( new Metric(getStart(), before != null ? before.getValue() : metric.getValue()) );
			}
			metrics.add( metric );
		}
		
		while( iter.hasNext() ) {
			millis = iter.next();
			metrics.add( new Metric(millis, result.getFloat(millis)) );
		}
		
		if( metrics.size() > 0 ) {
			metric = metrics.get( metrics.size()-1 );
			// If last value was not near the end, search beyond for the next value
			if( metric.getMillis() <= (getEnd() - resolution.getSeconds()*1000) ) {
				Metric after = findRangeExteriorMetric(compositeId, getEnd(), Long.MAX_VALUE, false );
				metrics.add( new Metric(getEnd(), after != null ? after.getValue() : metric.getValue()) );
			}
		}
		return metrics;
	}
	

	private Metric findRangeExteriorMetric( String compositeId, Long first, Long last, boolean reversed ) {
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(first, last, reversed, 1 );
		ColumnFamilyResult<String,Long> result = sequence.node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		Collection<Long> times = result.getColumnNames();
		if( times.size() > 0 ) {
			Long millis = times.iterator().next();
			return new Metric( millis, result.getFloat(millis) );
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((sequence == null) ? 0 : sequence.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CassandraMetricRange other = (CassandraMetricRange) obj;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		return true;
	}
	
}

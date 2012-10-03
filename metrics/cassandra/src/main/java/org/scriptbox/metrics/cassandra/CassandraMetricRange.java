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
import org.scriptbox.metrics.model.MetricSequence;

public class CassandraMetricRange extends MetricRange {

	private static ThreadLocal<Map<CassandraMetricRange,List<Metric>>> cache = new ThreadLocal<Map<CassandraMetricRange,List<Metric>>>();
	
	private MetricResolution resolution;
	private CassandraMetricSequence sequence;
	private List<Metric> metrics;
	
	static void begin() {
		cache.set( new HashMap<CassandraMetricRange,List<Metric>>() );
	}
	static void end() {
		cache.remove();
	}
	
	public CassandraMetricRange( CassandraMetricSequence sequence, long start, long end, MetricResolution resolution ) {
		super( start, end );
		this.sequence = sequence;
		this.resolution = resolution;
	}
	
	
	@Override
	public MetricResolution getResolution() {
		return resolution;
	}
	
	@Override
	public MetricSequence getSequence() {
		return sequence;
	}

	@Override
	public List<Metric> getMetrics() {
		if( metrics == null ) { 
			Map<CassandraMetricRange,List<Metric>> lcache = cache.get();
			if( lcache != null ) {
				List<Metric> cached = lcache.get( this );
				if( cached != null ) {
					return cached;
				}
			}
			
			fetchMetrics();
			
			if( lcache != null ) {
				lcache.put( this, metrics );
			}
		}
		return metrics;
	}
	
	private List<Metric> fetchMetrics() {
		String compositeId = sequence.node.getId() + "," + resolution.getSeconds();
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(getStart(), getEnd(), false, Integer.MAX_VALUE );
		ColumnFamilyResult<String,Long> result = sequence.node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		
		Collection<Long> times = result.getColumnNames();
		metrics = new ArrayList<Metric>( times.size() + 2 ); // 2 for the potential of adding an earlier and later value
		Iterator<Long> iter = times.iterator();
	
		Long millis = null;
		Metric metric = null;
		if( iter.hasNext() ) {
			millis = iter.next();
			metric = new Metric( millis, result.getFloat(millis) );
			// If first metric is not near the beginning, try to find last previous value
			if( millis >= (getStart() + getResolution().getSeconds()*1000) ) {
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
			if( metric.getMillis() <= (getEnd() - getResolution().getSeconds()*1000) ) {
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
	
	public Iterator<Metric> getConstantResolutionIterator() {
		return getMetrics().iterator();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((resolution == null) ? 0 : resolution.hashCode());
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
		if (resolution == null) {
			if (other.resolution != null)
				return false;
		} else if (!resolution.equals(other.resolution))
			return false;
		if (sequence == null) {
			if (other.sequence != null)
				return false;
		} else if (!sequence.equals(other.sequence))
			return false;
		return true;
	}

	
	
}

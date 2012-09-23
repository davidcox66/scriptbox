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
		Map<CassandraMetricRange,List<Metric>> lcache = cache.get();
		if( lcache != null ) {
			List<Metric> cached = lcache.get( this );
			if( cached != null ) {
				return cached;
			}
		}
		
		String compositeId = sequence.node.getId() + "," + getResolution();
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(getStart(), getEnd(), false, Integer.MAX_VALUE );
		ColumnFamilyResult<String,Long> result = sequence.node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		Collection<Long> times = result.getColumnNames();
		List<Metric> metrics = new ArrayList<Metric>( times.size() );
		for( Long millis : result.getColumnNames() ) {
			metrics.add( new Metric(millis, result.getFloat(millis)) );
		}
		return metrics;
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

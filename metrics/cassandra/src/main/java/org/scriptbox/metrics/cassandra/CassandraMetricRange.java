package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricCache;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.query.MetricQueryContext;

public class CassandraMetricRange extends MetricRange {

	private CassandraMetricSequence sequence;

	public CassandraMetricRange( CassandraMetricSequence sequence, long start, long end ) {
		super( sequence.node.getName(), start, end );
		this.sequence = sequence;
	}

	public String getName() {
		return sequence.node.getName();
	}

	public String getId( int seconds ) {
		return sequence.node.getId( seconds );
	}
	
	public int getNearestResolutionSeconds( int seconds ) {
		return sequence.getNearestResolutionSeconds(seconds);
	}
	
	public boolean isPersistent() {
		return true;
	}

	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return this;
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
			int resm = resolution * 1000;
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
				else if( pending == null ) {
					pending = iter.next();
				}
				
				if( pending.getMillis() < cend ) {
					// round to next resolution increment
					cstart += pending.getMillis() + resm - (pending.getMillis() % resm); 
					ret = last = pending;
					pending = null;
				}
				else {
					ret = new Metric(cstart, last.getValue() );
					cstart += resolution * 1000;
				}
				return ret;
			}
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported");
			}
		};
	}
	
	private List<Metric> fetchMetrics( int seconds ) {
		List<Metric> cached = MetricCache.get( this, seconds );
		if( cached != null ) {
			return cached;
		}
		
		List<Metric> metrics = queryMetrics( seconds );
		MetricCache.put( this, seconds, metrics );
		return metrics;
	}
	
	protected List<Metric> queryMetrics( int seconds ) {
		int resolution = sequence.getNearestResolutionSeconds(seconds);
		String compositeId = sequence.node.getId( resolution );
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(getStart(), getEnd(), false, Integer.MAX_VALUE );
		ColumnFamilyResult<String,Long> result = sequence.node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
	
		return sequence.node.tree.store.loadMetrics(getStart(), getEnd(), resolution, result );
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

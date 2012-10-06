package org.scriptbox.metrics.cassandra;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;

import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraMetricSequence extends MetricSequence {

	private static final Logger LOGGER = LoggerFactory.getLogger( CassandraMetricSequence.class );
	
	CassandraMetricTreeNode node;

	static class LastSaved
	{
		long millis;
		float value = Float.NaN;
		float accumulator;
		int count;
	}
	
	private Map<Integer,LastSaved> lastSavedValues = new HashMap<Integer,LastSaved>();
	
	public CassandraMetricSequence( CassandraMetricTreeNode node ) {
		this.node = node;
	}
	
	public void record( Metric metric ) {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "record: metric=" + metric ); }
		
		ColumnFamilyTemplate<String,Long> tmpl = node.getStore().metricSequenceTemplate;
		for( MetricResolution res : node.tree.getResolutions() ) {
			int resm = res.getSeconds() * 1000;
			LastSaved lastSaved = lastSavedValues.get( resm );
			if( lastSaved == null ) {
				lastSaved = new LastSaved();
				lastSavedValues.put( resm, lastSaved );
			}
			lastSaved.accumulator += metric.getValue();
			lastSaved.count++;
			
			if( lastSaved.value != metric.getValue() && (metric.getMillis() - resm) > lastSaved.millis ) 
			{
				float average = lastSaved.count == 1 ? lastSaved.accumulator : (lastSaved.accumulator / lastSaved.count);
				if( LOGGER.isDebugEnabled() ) { 
					LOGGER.debug( "record: saving metric=" + metric + ", count=" + lastSaved.count + ", average=" + average); 
				}
				ColumnFamilyUpdater<String,Long> updater = tmpl.createUpdater(node.getId()+","+res.getSeconds());
				updater.setFloat(new Long(metric.getMillis()), average);
				tmpl.update( updater );
				
				lastSaved.accumulator = 0;
				lastSaved.count = 0;
				lastSaved.millis = metric.getMillis();
				lastSaved.value = metric.getValue();
			}
			else {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "record: skipping metric=" + metric ); }
			}
		}
	}
	
	public DateRange getDateRange() {
		String compositeId = node.getId() + "," + node.tree.getFinestResolution().getSeconds();
		Date start = findMetricEdgeDate( compositeId, 0L, Long.MAX_VALUE, false );
		Date end = findMetricEdgeDate( compositeId, Long.MAX_VALUE, 0L, true );
		if( start != null && end != null ) {
			return new DateRange( start, end );
		}
		return null;
	}
	
	
	private Date findMetricEdgeDate( String compositeId, Long first, Long last, boolean reversed ) {
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(first, last, reversed, 1 );
		ColumnFamilyResult<String,Long> result = node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		Collection<Long> times = result.getColumnNames();
		if( times.size() > 0 ) {
			Long millis = times.iterator().next();
			return new Date( millis );
		}
		return null;
	}
	
	@Override
	public MetricRange getRange(long start, long end, int resolution) {
		MetricResolution res = node.tree.getResolutionEx( resolution ); 
		return new CassandraMetricRange(this, start, end, res );
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		CassandraMetricSequence other = (CassandraMetricSequence) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	
}

package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;

public class CassandraMetricSequence extends MetricSequence {

	CassandraMetricTreeNode node;

	
	static class LastSaved
	{
		float value = Float.NaN;
		long millis;
	}
	private Map<Integer,LastSaved> lastSavedValues = new HashMap<Integer,LastSaved>();
	
	public CassandraMetricSequence( CassandraMetricTreeNode node ) {
		this.node = node;
	}
	
	public void record( Metric metric ) {
		long now = System.currentTimeMillis();
		ColumnFamilyTemplate<String,Long> tmpl = node.getStore().metricSequenceTemplate;
		for( MetricResolution res : node.tree.getResolutions() ) {
			int resm = res.getMillis();
			LastSaved lastSaved = lastSavedValues.get( resm );
			boolean save = false;
			if( lastSaved != null ) {
				if( (lastSaved.value == Float.NaN || lastSaved.value != metric.getValue()) &&
					(now - res.getMillis()) > lastSaved.millis ) 
				{
					save = true;
				}
			}
			else {
				lastSaved = new LastSaved();
				save = true;
			}
			if( save ) {
				tmpl.createUpdater(node.getId()+","+res.getMillis()).setFloat(new Long(metric.getMillis()), metric.getValue());
				lastSaved.millis = now;
			}
		}
	}
	
	@Override
	public MetricRange getRange(long start, long end, int resolution) {
		if( !node.tree.isValidResolution(resolution) ) {
			throw new RuntimeException( "Invalid resolution '" + resolution + "' for '" + node.getId() + "'" );
		}
		String compositeId = node.getId() + "," + resolution;
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(start, end, false, Integer.MAX_VALUE );
		ColumnFamilyResult<String,Long> result = node.getStore().metricSequenceTemplate.queryColumns( compositeId, predicate );
		Collection<Long> times = result.getColumnNames();
		List<Metric> metrics = new ArrayList<Metric>( times.size() );
		for( Long millis : result.getColumnNames() ) {
			metrics.add( new Metric(millis, result.getFloat(millis)) );
		}
		return null;
	}

	@Override
	public void deleteAllRange(long start, long end, int resolution) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllRangeResolutions(long start, long end) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((lastSavedValues == null) ? 0 : lastSavedValues.hashCode());
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
		if (lastSavedValues == null) {
			if (other.lastSavedValues != null)
				return false;
		} else if (!lastSavedValues.equals(other.lastSavedValues))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	
}

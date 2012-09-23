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
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;

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
		ColumnFamilyTemplate<String,Long> tmpl = node.getStore().metricSequenceTemplate;
		for( MetricResolution res : node.tree.getResolutions() ) {
			int resm = res.getSeconds() * 1000;
			LastSaved lastSaved = lastSavedValues.get( resm );
			boolean save = false;
			if( lastSaved != null ) {
				if( (lastSaved.value == Float.NaN || lastSaved.value != metric.getValue()) &&
					(metric.getMillis() - resm) > lastSaved.millis ) 
				{
					save = true;
				}
			}
			else {
				lastSaved = new LastSaved();
				lastSavedValues.put( resm, lastSaved );
				save = true;
			}
			if( save ) {
				ColumnFamilyUpdater<String,Long> updater = tmpl.createUpdater(node.getId()+","+res.getSeconds());
				updater.setFloat(new Long(metric.getMillis()), metric.getValue());
				tmpl.update( updater );
				lastSaved.millis = metric.getMillis();
			}
		}
	}
	
	@Override
	public MetricRange getRange(long start, long end, int resolution) {
		MetricResolution res = node.tree.getResolutionEx( resolution ); 
		return new CassandraMetricRange(this, start, end, res );
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

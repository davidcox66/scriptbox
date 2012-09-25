package org.scriptbox.metrics.cassandra;

import java.util.HashMap;
import java.util.Map;

import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraMetricSequence extends MetricSequence {

	private static final Logger LOGGER = LoggerFactory.getLogger( CassandraMetricSequence.class );
	
	CassandraMetricTreeNode node;

	private Map<Integer,Metric> lastSavedValues = new HashMap<Integer,Metric>();
	
	public CassandraMetricSequence( CassandraMetricTreeNode node ) {
		this.node = node;
	}
	
	public void record( Metric metric ) {
		if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "record: metric=" + metric ); }
		
		ColumnFamilyTemplate<String,Long> tmpl = node.getStore().metricSequenceTemplate;
		for( MetricResolution res : node.tree.getResolutions() ) {
			int resm = res.getSeconds() * 1000;
			Metric lastSaved = lastSavedValues.get( resm );
			if( lastSaved == null || 
				(lastSaved.getValue() != metric.getValue() &&
				(metric.getMillis() - resm) > lastSaved.getMillis()) ) 
			{
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "record: saving metric=" + metric ); }
				ColumnFamilyUpdater<String,Long> updater = tmpl.createUpdater(node.getId()+","+res.getSeconds());
				updater.setFloat(new Long(metric.getMillis()), metric.getValue());
				tmpl.update( updater );
				lastSavedValues.put( resm, metric );
			}
			else {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "record: skipping metric=" + metric ); }
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

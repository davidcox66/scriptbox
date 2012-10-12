package org.scriptbox.metrics.compute;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class ListBackedMetricRange extends MetricRange {

	private List<Metric> metrics;
	
	public ListBackedMetricRange( String description, long start, long end, List<Metric> metrics ) {
		super( description, start, end );
		this.metrics = metrics;
	}

	public String getName() {
		return getDescription();
	}
	
	public boolean isPersistent() {
		return false;
	}

	@Override
	public List<Metric> getMetrics(int resolution) {
		return metrics;
	}

	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return this;
	}

	public DateRange getFullDateRange() {
		if( metrics.size() > 0 ) {
			return new DateRange( new Date(metrics.get(0).getMillis()), new Date(metrics.get(metrics.size()-1).getMillis()) );
		}
		return null;
	}
	@Override
	public Iterator<Metric> getIterator(int resolution) {
		return metrics.iterator();
	}

}

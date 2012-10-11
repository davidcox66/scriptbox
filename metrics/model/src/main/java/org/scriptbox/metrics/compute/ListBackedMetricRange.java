package org.scriptbox.metrics.compute;

import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class ListBackedMetricRange extends MetricRange {

	private List<Metric> metrics;
	
	public ListBackedMetricRange( String description, long start, long end, List<Metric> metrics ) {
		super( description, start, end );
		this.metrics = metrics;
	}
	
	@Override
	public List<Metric> getMetrics(int resolution) {
		return metrics;
	}

	@Override
	public Iterator<Metric> getIterator(int resolution) {
		return metrics.iterator();
	}

}

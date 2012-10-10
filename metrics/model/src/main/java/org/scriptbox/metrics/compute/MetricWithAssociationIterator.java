package org.scriptbox.metrics.compute;

import java.util.Iterator;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class MetricWithAssociationIterator<X> implements Iterator<Metric> {

	private X associate;
	private Iterator<Metric> metrics;
	
	public MetricWithAssociationIterator( X associate, Iterator<Metric> metrics ) {
		this.associate = associate;
		this.metrics = metrics;
	}
	
	public boolean hasNext() {
		return metrics.hasNext();
	}
	public Metric next() {
		Metric metric = metrics.next();
		return new MetricWithAssociation<X>( metric.getMillis(), metric.getValue(), associate );
	}
	public void remove() {
		throw new UnsupportedOperationException("remove() not supported");
	}
}

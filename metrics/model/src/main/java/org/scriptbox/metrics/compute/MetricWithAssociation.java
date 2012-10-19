package org.scriptbox.metrics.compute;

import java.io.Serializable;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.Timed;

public final class MetricWithAssociation<X> implements Serializable, Timed {

	private static final long serialVersionUID = 1L;
	
	private Metric metric;
	private X associate;
	
	public MetricWithAssociation() {
	}
	
	public MetricWithAssociation( Metric metric, X associate ) {
		this.metric = metric;
		this.associate = associate;
	}

	public final long getMillis() {
		return metric.getMillis();
	}
	
	public final Metric getMetric() {
		return metric;
	}

	public final void setMetric(Metric metric) {
		this.metric = metric;
	}

	public final X getAssociate() {
		return associate;
	}

	public final void setAssociate(X associate) {
		this.associate = associate;
	}

	public String toString() {
		return "MetricWithAssociation{ metric=" + metric + ", associate=" + associate + " }";
	}
}

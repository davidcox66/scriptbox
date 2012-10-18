package org.scriptbox.metrics.compute;

import java.io.Serializable;

import org.scriptbox.metrics.model.Metric;

public class MetricWithAssociation<X> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Metric metric;
	private X associate;
	
	public MetricWithAssociation() {
	}
	
	public MetricWithAssociation( Metric metric, X associate ) {
		this.metric = metric;
		this.associate = associate;
	}

	public Metric getMetric() {
		return metric;
	}

	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	public X getAssociate() {
		return associate;
	}

	public void setAssociate(X associate) {
		this.associate = associate;
	}
	
}

package org.scriptbox.metrics.compute;

import org.scriptbox.metrics.model.Metric;

public class MetricWithAssociation<X> extends Metric {

	private static final long serialVersionUID = 1L;
	
	private X associate;
	
	public MetricWithAssociation() {
	}
	
	public MetricWithAssociation( long millis, float value, X associate ) {
		super( millis, value );
		this.associate = associate;
	}

	public X getAssociate() {
		return associate;
	}

	
	
}

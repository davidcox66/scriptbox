package org.scriptbox.metrics.query.main;

public class MetricException extends Exception {

	private static final long serialVersionUID = 1L;

	public MetricException(String message) {
		super(message);
	}

	public MetricException(String message, Throwable cause) {
		super(message, cause);
	}

}

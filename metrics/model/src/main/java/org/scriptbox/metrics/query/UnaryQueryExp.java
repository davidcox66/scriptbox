package org.scriptbox.metrics.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UnaryQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnaryQueryExp.class);

	private String operator;
	private MetricQueryExp child;

	public UnaryQueryExp(String operator, MetricQueryExp child) {
		this.operator = operator;
		this.child = child;
	}

	public Object evaluate(MetricQueryContext ctx) {
		Object result = child.evaluate(ctx);
		Object ret = process(result, ctx);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("evaluate(unary): " + toString() + " result=" + ret);
		}
		return ret;
	}

	public abstract Object process(Object result, MetricQueryContext ctx);

	public String toString() {
		return operator + "(" + child + ")";
	}
}

package org.scriptbox.metrics.query;

import org.apache.commons.collections.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BinaryQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinaryQueryExp.class);

	private String operator;
	private MetricQueryExp lhs;
	private MetricQueryExp rhs;

	public BinaryQueryExp(String operator, MetricQueryExp lhs, MetricQueryExp rhs) {
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Object evaluate(MetricQueryContext ctx) {
		Object lresult = lhs.evaluate(ctx);
		Object rresult = rhs.evaluate(ctx);
		Object ret = process(lresult, rresult, ctx);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("evaluate(binary): " + toString() + "lresult=" + lresult + ", rresult=" + rresult + ", result=" + ret);
		}
		return ret;
	}

	public abstract Object process(Object lresult, Object rresult, MetricQueryContext ctx);

	public String toString() {
		return operator + "(" + lhs + "," + rhs + ")";
	}
}

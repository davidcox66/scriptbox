package org.scriptbox.metrics.query.exp;

import org.scriptbox.metrics.query.main.MetricException;
import org.scriptbox.metrics.query.main.MetricQueryContext;
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

	public Object evaluate(MetricQueryContext ctx) throws Exception {
		Object result = child.evaluate(ctx);
		if( result == null ) {
			throw new MetricException( "No metrics for child: " + child );
		}
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

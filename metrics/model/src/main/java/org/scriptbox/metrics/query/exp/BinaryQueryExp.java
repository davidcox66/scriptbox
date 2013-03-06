package org.scriptbox.metrics.query.exp;

import org.scriptbox.metrics.query.main.MetricException;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for expression which have 2 children that are evaluated and
 * have a particular operation applied to achieve  a result. This just
 * removes some of the boiler-plate code to make that happen. Sublcasses
 * must override process() to perform this specific operation.
 * 
 * @author david
 *
 */
public abstract class BinaryQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(BinaryQueryExp.class);

	private String name;
	private String operator;
	private MetricQueryExp lhs;
	private MetricQueryExp rhs;

	public BinaryQueryExp(String name, String operator, MetricQueryExp lhs, MetricQueryExp rhs) {
		this.name = name;
		this.operator = operator;
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Object evaluate(MetricQueryContext ctx) throws Exception {
		Object lresult = lhs.evaluate(ctx);
		Object rresult = rhs.evaluate(ctx);
		if( lresult == null ) {
			throw new MetricException( "No metrics for lhs: " + lhs );
		}
		if( rresult == null ) {
			throw new MetricException( "No metrics for rhs: " + rhs );
		}
		Object ret = process(lresult, rresult, ctx);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("evaluate(binary): " + toString() + ", lresult=" + lresult + ", rresult=" + rresult + ", result=" + ret);
		}
		return ret;
	}

	public abstract Object process(Object lresult, Object rresult, MetricQueryContext ctx);

	public String toString() {
		return name != null ? name : operator + "(" + lhs + "," + rhs + ")";
	}
}

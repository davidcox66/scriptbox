package org.scriptbox.metrics.query.exp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.scriptbox.metrics.query.main.MetricException;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NaryQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(NaryQueryExp.class);

	private String operator;
	private List<MetricQueryExp> expressions;

	public NaryQueryExp(String operator, MetricQueryExp...expressions) {
		this.operator = operator;
		this.expressions = Arrays.asList(expressions);
	}

	public Object evaluate(MetricQueryContext ctx) throws Exception {
		List<Object> results = new ArrayList<Object>( expressions.size() );
		for( MetricQueryExp exp : expressions ) {
			Object result = exp.evaluate( ctx );
			if( result == null ) {
				throw new MetricException( "No metrics for expression: " + exp );
			}
			results.add( result );
		}
		Object ret = process(results, ctx);
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("evaluate(n-ary): " + toString() + ", results=" + results);
		}
		return ret;
	}

	public abstract Object process(List<Object> results, MetricQueryContext ctx);

	public String toString() {
		return operator + "(" + expressions + ")";
	}
}

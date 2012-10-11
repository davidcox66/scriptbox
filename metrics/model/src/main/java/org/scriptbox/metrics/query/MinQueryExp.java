package org.scriptbox.metrics.query;

import java.util.Iterator;
import java.util.Map;

import org.scriptbox.metrics.compute.MetricCollator;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinQueryExp.class);

	private MetricQueryExp child;

	public MinQueryExp(MetricQueryExp child) {
		this.child = child;
	}

	public Object evaluate(final MetricQueryContext ctx) throws Exception {
	    Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.evaluateProviders(ctx, child);
		MetricCollator collator = new MetricCollator("min", ctx.getChunk(),metrics.values());
		return collator.collate(false, new ParameterizedRunnableWithResult<Metric, MetricRange>() {
			public Metric run(MetricRange range) {
				Iterator<Metric> iter = range.getIterator(ctx.getChunk());
				float min = Float.MAX_VALUE;
				while (iter.hasNext()) {
					Metric metric = iter.next();
					min = Math.min(metric.getValue(), min);
				}
				return new Metric(range.getStart(), min);
			}
		});
	}

	public String toString() {
		return "min(" + child + ")";
	}
}

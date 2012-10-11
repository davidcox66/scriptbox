package org.scriptbox.metrics.query;

import java.util.Iterator;
import java.util.Map;

import org.scriptbox.metrics.compute.MetricCollator;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;

public class TotalPerSecondQueryExp implements MetricQueryExp {

	private MetricQueryExp child;

	public TotalPerSecondQueryExp(MetricQueryExp child) {
		this.child = child;
	}

	public Object evaluate(final MetricQueryContext ctx) throws Exception {
		final int seconds = ctx.getChunk() / 1000;
		Map<? extends MetricProvider, ? extends MetricRange> metrics = MetricQueries.evaluateProviders(ctx, child);
		MetricCollator collator = new MetricCollator("tps", ctx.getChunk(), metrics.values());
		return collator.collate(false, new ParameterizedRunnableWithResult<Metric, MetricRange>() {
			public Metric run(MetricRange range) {
				Iterator<Metric> iter = range.getIterator(ctx.getChunk());
				long total = 0;
				while (iter.hasNext()) {
					Metric metric = iter.next();
					total += metric.getValue();
				}
				return new Metric(range.getStart(), total / seconds);
			}
		});
	}

	public String toString() {
		return "tps(" + child + ")";
	}

}

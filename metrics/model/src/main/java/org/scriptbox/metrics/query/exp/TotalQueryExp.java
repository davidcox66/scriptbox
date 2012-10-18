package org.scriptbox.metrics.query.exp;

import java.util.Iterator;
import java.util.Map;

import org.scriptbox.metrics.compute.MetricCollator;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotalQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(TotalQueryExp.class);

	private MetricQueryExp child;

	public TotalQueryExp(MetricQueryExp child) {
		this.child = child;
	}

	public Object evaluate(final MetricQueryContext ctx) throws Exception {
	    Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.providers(ctx, child);
		MetricCollator collator = new MetricCollator("total", "total", ctx.getChunk(),metrics.values());
		return collator.collate(false, new ParameterizedRunnableWithResult<Metric, MetricRange>() {
			public Metric run(MetricRange range) {
				Iterator<Metric> iter = range.getIterator(ctx.getChunk());
				float total = 0;
				while (iter.hasNext()) {
					Metric metric = iter.next();
					total += metric.getValue();
				}
				return new Metric(range.getStart(), total);
			}
		});

	}

	public String toString() {
		return "total(" + child + ")";
	}
}

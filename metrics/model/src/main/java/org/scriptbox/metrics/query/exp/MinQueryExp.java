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

public class MinQueryExp implements MetricQueryExp {

	private static final Logger LOGGER = LoggerFactory.getLogger(MinQueryExp.class);

	private String name;
	private MetricQueryExp child;

	public MinQueryExp(MetricQueryExp child) {
		this( null, child );
	}
	public MinQueryExp( String name, MetricQueryExp child) {
		this.name = name;
		this.child = child;
	}

	public Object evaluate(final MetricQueryContext ctx) throws Exception {
	    Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.providers(ctx, child);
	    String label = toString();
		MetricCollator collator = new MetricCollator( label, label, ctx.getResolution(),metrics.values());
		return collator.collate(false, new ParameterizedRunnableWithResult<Metric, MetricRange>() {
			public Metric run(MetricRange range) {
				Iterator<Metric> iter = range.getIterator(ctx.getResolution());
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
		return name != null ? name : "min(" + child + ")";
	}
}

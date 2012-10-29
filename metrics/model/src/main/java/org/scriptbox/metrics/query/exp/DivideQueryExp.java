package org.scriptbox.metrics.query.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scriptbox.metrics.compute.ListBackedMetricRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class DivideQueryExp implements MetricQueryExp {

	private String name;
	private MetricQueryExp child;
	private float divisor;

	public DivideQueryExp(float divisor, MetricQueryExp child) {
		this( null, divisor, child );
	}
	public DivideQueryExp(String name, float divisor, MetricQueryExp child) {
		this.name = name;
		this.child = child;
		this.divisor = divisor;
		if( divisor == 0 ) {
			throw new IllegalArgumentException( "Divisor cannot be zero" );
		}
	}

	public Object evaluate(MetricQueryContext ctx) throws Exception {
		Set<MetricProvider> ret = new HashSet<MetricProvider>();
		Map<? extends MetricProvider, ? extends MetricRange> metrics = MetricQueries.providers(ctx, child);
		for (MetricRange range : metrics.values() ) {
			List<Metric> values = new ArrayList<Metric>();
			Iterator<Metric> iter = range.getIterator(ctx.getResolution());
			while (iter.hasNext()) {
				Metric metric = iter.next();
				values.add(new Metric(metric.getMillis(), metric.getValue() / divisor));
			}
			String label = toString();
			ret.add(new ListBackedMetricRange(label,label,
				range.getFullDateRange(), range.getStart(), range .getEnd(), values));
		}
		return ret;
	}

	public String toString() {
		return name != null ? name : "divide(" + child + ")";
	}
}

package org.scriptbox.metrics.query.exp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.compute.MetricCollator;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;

public class DiffQueryExp implements MetricQueryExp {

	  private int chunk;
	  private MetricQueryExp child1; 
	  private MetricQueryExp child2; 
	  
	  public DiffQueryExp( int chunk, MetricQueryExp child1, MetricQueryExp child2 ) {
	    this.chunk = chunk;
	    this.child1 = child1;
	    this.child2 = child2;
	  }
	  public Object evaluate( final MetricQueryContext ctx ) throws Exception {
	      MetricProvider provider1 = MetricQueries.provider(ctx, child1 );
	      MetricProvider provider2 = MetricQueries.provider(ctx, child2 );

	      List<MetricRange> metrics = new ArrayList<MetricRange>(2);
	      metrics.add( provider1.getMetrics(ctx) );
	      metrics.add( provider2.getMetrics(ctx) );
	      
			MetricCollator collator = new MetricCollator("diff", "diff", chunk, metrics);
			return collator.group(new ParameterizedRunnableWithResult<Metric, List<MetricRange>>() {
				public Metric run(List<MetricRange> ranges) {
					float avg1 = computeAverage( ranges.get(0) );
					float avg2 = computeAverage( ranges.get(1) );
					float diff = avg1 - avg2;
					return new Metric( ranges.get(0).getStart(), diff );
				}
			});
	  }

	  private float computeAverage( MetricRange range ) {
		  int count = 0;
	      float accum = 0;
	      for( Iterator<Metric> iter = range.getIterator(chunk) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	          accum += metric.getValue();
	      }
		  return count > 0 ? accum / count : 0;
	  }
	  
	  public String toString() {
	    return "diff(child1=" + child1 + ", child2=" + child2 + ")";
	  }
}

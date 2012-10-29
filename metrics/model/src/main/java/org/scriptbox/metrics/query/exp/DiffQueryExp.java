package org.scriptbox.metrics.query.exp;

import java.util.Iterator;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class DiffQueryExp extends ComputeQueryExp {

	  public DiffQueryExp( int chunk, MetricQueryExp child1, MetricQueryExp child2 ) {
		  this( "diff", chunk, child1, child2 );
	  }

	  public DiffQueryExp( String name, int chunk, MetricQueryExp child1, MetricQueryExp child2 ) {
		  super( name, "-", chunk, child1, child2 );
	  }
	  protected float compute( MetricRange range ) {
		  int count = 0;
	      float accum = 0;
	      for( Iterator<Metric> iter = range.getIterator(chunk) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	          accum += metric.getValue();
	      }
		  return count > 0 ? accum / count : 0;
	  }
	  protected float compute( float val1, float val2 ) {
		  return val1 - val2;
	  }
}

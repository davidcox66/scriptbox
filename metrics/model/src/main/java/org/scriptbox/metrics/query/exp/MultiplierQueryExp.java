package org.scriptbox.metrics.query.exp;

import java.util.Iterator;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class MultiplierQueryExp extends ComputeQueryExp {

	  public MultiplierQueryExp( int chunk, MetricQueryExp child1, MetricQueryExp child2 ) {
		  super( "multiplier", "*", chunk, child1, child2 );
	  }

	  protected float compute( MetricRange range ) {
	      float accum = 0;
	      for( Iterator<Metric> iter = range.getIterator(chunk) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	          accum += metric.getValue();
	      }
		  return accum;
	  }
	 
	  protected float compute( float val1, float val2 ) {
		  return val1 * val2;
	  }
}

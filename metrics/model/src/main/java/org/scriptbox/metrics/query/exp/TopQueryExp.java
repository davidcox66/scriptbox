package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class TopQueryExp extends FilteringQueryExp {

	  public TopQueryExp( int count, MetricQueryExp child ) {
	    this( "top", count, child );
	  }
	  public TopQueryExp( String name, int count, MetricQueryExp child ) {
	    super( name, count, child );
	  }
	  float filter( MetricRange range, MetricQueryContext ctx ) {
	      float max = Float.MIN_VALUE;
	      for( Iterator<Metric> iter = range.getIterator(ctx.getResolution()) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	          max = Math.max( max, metric.getValue() );
	      }
	      return max;
	  }
	  void sort( List<CriticalValue> cvs ) {
		  Collections.sort( cvs, new Comparator<CriticalValue>() {
			  public int compare( CriticalValue cv1, CriticalValue cv2 ) {
				  return (int)(cv2.value - cv1.value);
			  }
		  });
	  }
}

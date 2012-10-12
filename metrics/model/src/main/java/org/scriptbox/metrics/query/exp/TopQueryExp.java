package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class TopQueryExp extends FilteringQueryExp {

	  public TopQueryExp( int count, MetricQueryExp child ) {
	    super( "top", count, child );
	  }
	  float filter( MetricRange range ) {
	      float max = Float.MIN_VALUE;
	      for( Iterator<Metric> iter = range.getIterator(0) ; iter.hasNext() ; ) {
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

package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class BottomQueryExp extends FilteringQueryExp {

	  public BottomQueryExp( int count, MetricQueryExp child ) {
	    super( "bottom", count, child );
	  }
	  float filter( MetricRange range ) {
	      float min = Float.MAX_VALUE;
	      for( Iterator<Metric> iter = range.getIterator(0) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	          min = Math.min( min, metric.getValue() );
	      }
	      return min;
	  }
	  void sort( List<CriticalValue> cvs ) {
		  Collections.sort( cvs, new Comparator<CriticalValue>() {
			  public int compare( CriticalValue cv1, CriticalValue cv2 ) {
				  return (int)(cv1.value - cv2.value);
			  }
		  });
	  }
}

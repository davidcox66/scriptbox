package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class BottomQueryExp extends FilteringQueryExp {

	  public BottomQueryExp( int count, MetricQueryExp child ) {
	    this( "bottom", count, child );
		  
	  }
	  public BottomQueryExp( String name, int count, MetricQueryExp child ) {
	    super( name, count, child );
	  }
	  
	  float filter( MetricRange range, MetricQueryContext ctx ) {
	      float min = Float.MAX_VALUE;
	      for( Iterator<Metric> iter = range.getIterator(ctx.getResolution()) ; iter.hasNext() ; ) {
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

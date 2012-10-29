package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;

public class TopAverageQueryExp extends FilteringQueryExp {

	  public TopAverageQueryExp( int count, MetricQueryExp child ) {
		  this( "topavg", count, child );
	  }
	  public TopAverageQueryExp( String name, int count, MetricQueryExp child ) {
	    super( name, count, child );
	  }
	  float filter( MetricRange range ) {
		  int count = 0;
	      float total = 0;
	      for( Iterator<Metric> iter = range.getIterator(0) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	    	  total += metric.getValue();
	    	  count++;
	      }
	      return count > 0 ? total / count : 0;
	  }
	  void sort( List<CriticalValue> cvs ) {
		  Collections.sort( cvs, new Comparator<CriticalValue>() {
			  public int compare( CriticalValue cv1, CriticalValue cv2 ) {
				  return (int)(cv2.value - cv1.value);
			  }
		  });
	  }
}

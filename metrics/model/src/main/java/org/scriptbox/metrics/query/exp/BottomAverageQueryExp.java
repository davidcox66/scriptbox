package org.scriptbox.metrics.query.exp;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class BottomAverageQueryExp extends FilteringQueryExp {

	  public BottomAverageQueryExp( int count, MetricQueryExp child ) {
	    this( "bottomavg", count, child );
	  }
	  public BottomAverageQueryExp( String name, int count, MetricQueryExp child ) {
	    super( name, count, child );
	  }
	  
	  float filter( MetricRange range, MetricQueryContext ctx ) {
		  int count = 0;
	      float total = 0;
	      for( Iterator<Metric> iter = range.getIterator(ctx.getResolution()) ; iter.hasNext() ; ) {
	    	  Metric metric = iter.next();
	    	  total += metric.getValue();
	    	  count++;
	      }
	      return count > 0 ? total / count : 0;
	  }
	  void sort( List<CriticalValue> cvs ) {
		  Collections.sort( cvs, new Comparator<CriticalValue>() {
			  public int compare( CriticalValue cv1, CriticalValue cv2 ) {
				  return (int)(cv1.value - cv2.value);
			  }
		  });
	  }
}

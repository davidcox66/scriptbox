package org.scriptbox.metrics.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.model.MetricRange;

public abstract class FilteringQueryExp implements MetricQueryExp {

	  private String operator; 
	  private MetricQueryExp child; 
	  private int count;

	  public FilteringQueryExp( String operator, int count, MetricQueryExp child ) {
	    this.operator = operator;
	    this.count = count;
	    this.child = child;
	  }
	  
	  abstract double findCriticalValue( MetricRange range );
	  abstract void sort( List<SourceMetricCriticalValue> cvs );
	  
	  public Object evaluate( MetricQueryContext ctx ) {
		  
		  MetricTreeNode node = child.evaluate()
	      Map<SourceMetric,List<MetricValue>> metrics = ValueGrouper.toValues( child.evaluate(ctx), ctx );
	      List<SourceMetricCriticalValue> cvs = new ArrayList<SourceMetricCriticalValue>();
	      metrics.each{ SourceMetric metric, List<MetricValue> values ->
	          double cv = findCriticalValue( values );
	          cvs.add( new SourceMetricCriticalValue(metric:metric,value:cv) ) ;
	      }
	      sort( cvs );
	      int countOrLast = Math.min(count,cvs.size()) - 1;
	      return new HashSet<SourceMetric>(cvs[0..countOrLast].collect{ SourceMetricCriticalValue  cv -> cv.metric } );
	  }
	  String toString() {
	    return "${name}(${child})";
	  }
	}
	
	
	
	
	@Override
	public Object evaluate(MetricQueryContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

}

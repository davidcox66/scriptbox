package org.scriptbox.metrics.query.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.metrics.query.main.MetricQueryContext;

/**
 * This is the base class or expression which collate result over time, performing
 * a specific aggregation function such as a sum or average, sorting these aggregates
 * then the top/bottom X set of metrics ranges from these. For example, the
 * BottomAverageQueryExp expression can be given a list of various related metrics,
 * find those that have the lowest average value, then pick a specified number of
 * these to be the result of the expression.
 * 
 * @author david
 *
 */
public abstract class FilteringQueryExp implements MetricQueryExp {

	  private String operator; 
	  private MetricQueryExp child; 
	  private int count;

	  public static class CriticalValue 
	  {
		  public CriticalValue( MetricProvider provider, float value ) {
			  this.provider = provider;
			  this.value = value;
		  }
		  
		  public MetricProvider provider;
		  public float value;
	  }
	  
	  public FilteringQueryExp( String operator, int count, MetricQueryExp child ) {
	    this.operator = operator;
	    this.count = count;
	    this.child = child;
	  }
	
	  /**
	   * Performs a particular aggregating function on the underlying data such as
	   * a sum or average
	   * @param range
	   * @return
	   */
	  abstract float filter( MetricRange range );
	  
	  /**
	   * Received the results from filtering of the data and sorts them as needed.
	   * The first 'count' number of metric ranges will be the result of
	   * evaluating this expression. 
	   * @param cvs
	   */
	  abstract void sort( List<CriticalValue> cvs );
	  
	  public Object evaluate( MetricQueryContext ctx ) throws Exception {
		 
		  Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.providers(ctx, child);
	      List<CriticalValue> cvs = new ArrayList<CriticalValue>(metrics.size());
	      for( Map.Entry<? extends MetricProvider,? extends MetricRange> entry : metrics.entrySet() ) {
	    	  float cv = filter( entry.getValue() );
	    	  cvs.add( new CriticalValue(entry.getKey(),cv) );
	      }
	      sort( cvs );
	      int countOrLast = Math.min(count,cvs.size()) - 1;
	      if( countOrLast > 0 ) {
		      Set<MetricProvider> ret = new HashSet<MetricProvider>(countOrLast);
		      for( int i=0 ; i < countOrLast ; i++ ) {
		    	  ret.add( cvs.get(i).provider );
		      }
		      return ret;
	      }
	      return null;
	  }
	  
	  public String toString() {
	    return operator + "(" + child + ")";
	  }
}
	
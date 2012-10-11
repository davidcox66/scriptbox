package org.scriptbox.metrics.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scriptbox.metrics.model.MetricRange;

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
	  
	  abstract float filter( MetricRange range );
	  abstract void sort( List<CriticalValue> cvs );
	  
	  public Object evaluate( MetricQueryContext ctx ) throws Exception {
		 
		  Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.evaluateProviders(ctx, child);
	      List<CriticalValue> cvs = new ArrayList<CriticalValue>();
	      for( Map.Entry<? extends MetricProvider,? extends MetricRange> entry : metrics.entrySet() ) {
	    	  float cv = filter( entry.getValue() );
	    	  cvs.add( new CriticalValue(entry.getKey(),cv) );
	      }
	      sort( cvs );
	      int countOrLast = Math.min(count,cvs.size()) - 1;
	      Set<MetricProvider> ret = new HashSet<MetricProvider>(countOrLast);
	      for( int i=0 ; i < countOrLast ; i++ ) {
	    	  ret.add( cvs.get(i).provider );
	      }
	      return ret;
	  }
	  
	  public String toString() {
	    return operator + "(" + child + ")";
	  }
}
	
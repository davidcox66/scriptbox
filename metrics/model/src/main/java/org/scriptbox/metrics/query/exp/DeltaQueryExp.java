package org.scriptbox.metrics.query.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scriptbox.metrics.compute.ListBackedMetricRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeltaQueryExp implements MetricQueryExp {

	 private static final Logger LOGGER = LoggerFactory.getLogger( DeltaQueryExp.class );
	
	  private String name;
	  private MetricQueryExp child; 
	  
	  public DeltaQueryExp( MetricQueryExp child ) {
	    this( null, child );
	  }
	  public DeltaQueryExp( String name, MetricQueryExp child ) {
	    this.name = name;
	    this.child = child;
	  }
	  
	  public Object evaluate( MetricQueryContext ctx ) throws Exception {
		  Set<MetricProvider> ret = new HashSet<MetricProvider>();
		  Map<? extends MetricProvider,? extends MetricRange> metrics = MetricQueries.providers(ctx, child);
		  for (MetricRange range : metrics.values() ) {
			  List<Metric> deltas = new ArrayList<Metric>();
		      Metric prev = null;
		      Iterator<Metric> iter = range.getIterator( ctx.getResolution() );
		      while( iter.hasNext() ) {
		    	 Metric metric = iter.next(); 
		    	 if( prev != null ) {
	                  float diff = metric.getValue() - prev.getValue();
	                  long sec = (metric.getMillis() - prev.getMillis()) / 1000;
	                  // don't really see this as a possibility but just being anal retentive
	                  if( sec < 1 ) { 
	                      sec = 1; 
	                  }
		              deltas.add( new Metric(metric.getMillis(), diff / sec) );
		    	 }
		    	 else {
		              deltas.add( new Metric(metric.getMillis(), 0) );
		    	 }
		    	 prev = metric;
		      }
		      String label = toString();
		      ret.add( new ListBackedMetricRange( label, label, 
		          range.getFullDateRange(), range.getStart(), range.getEnd(), deltas) );
		  }
		  return ret;
	  }
	  public String toString() {
	    return name != null ? name : "delta(" + child + ")";
	  }
}

package org.scriptbox.metrics.compute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.CollatingIterator;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricCollator {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricCollator.class );
	
	private String description;
	private int seconds;
	private List<MetricRange> ranges;
	
	public MetricCollator( String description, int seconds, List<MetricRange> ranges ) {
		this.description = description;
		this.seconds = seconds;
		this.ranges = ranges;
	}

	public MetricRange group( final ParameterizedRunnableWithResult<Metric,List<MetricRange>> closure ) throws Exception {
		
	    // Preload collections to store values from the grouping. We try to reuse these temporary collections
	    // as much as possible instead of constantly allocating new ones since the given closure will not
	    // be retaining what we supply it.  
		final List<MetricRange> rangesByReferenceNumber = new ArrayList<MetricRange>(ranges.size());
	    final List<List<Metric>> metricsByReferenceNumber = new ArrayList<List<Metric>>(ranges.size());
	    long start = Long.MAX_VALUE;
	    long end = 0;
	    
	    for( MetricRange range : ranges ) {
	    	start = Math.min(start, range.getStart() );
	    	end = Math.max(end, range.getEnd() );
	    }
	    for( MetricRange range :ranges ) {
	    	List<Metric> metrics = new ArrayList<Metric>();    
	    	metricsByReferenceNumber.add( metrics );    
	    	rangesByReferenceNumber.add( new ListBackedMetricRange(range.getDescription(),start,end,metrics) );
	    }
	     
	    return collate( true, new ParameterizedRunnableWithResult<Metric,MetricRange>() {
	    	public Metric run( MetricRange range ) throws Exception {
	    		Iterator<MetricWithAssociation<Integer>> iter = (Iterator)range.getIterator(seconds);
    			while( iter.hasNext() ) {
    				MetricWithAssociation<Integer> metric = iter.next();
    				//
	              	// Filter each metric back into individual collections for each supplied list of metrics.
	              	// This is done by using the MetricValue.reference which was assigned an incrementing
	              	// number above.
	              	//
		          	metricsByReferenceNumber.get(metric.getAssociate()).add( metric );
	          	}
    			Metric result = closure.run( rangesByReferenceNumber );
         
    			// Clear and reuse instead of making GC bear the brunt 
    			for( List<Metric> metrics : metricsByReferenceNumber ) {
    				metrics.clear(); 
    			}
    			return result;
	    	}
      	} );
	}

	public MetricRange collate( boolean associate, ParameterizedRunnableWithResult<Metric,MetricRange> closure ) throws Exception {

		final List<Metric> ret = new ArrayList<Metric>();
		CollatingIterator citer = new CollatingIterator( new Comparator<Metric>() {
			public int compare( Metric mv1, Metric mv2 ) {
		        return (int)(mv1.getMillis() - mv2.getMillis());
			}
		} );
	
		int i=0;
		for( MetricRange range : ranges ) {
			Iterator<Metric> iter = range.getIterator( seconds );
			citer.addIterator( associate ? new MetricWithAssociationIterator<Integer>(i++,iter) : iter );
		}
		
		final List<Metric> block = new ArrayList<Metric>();
		long current = 0;
		while( citer.hasNext() ) {
			Metric mv = (Metric)citer.next();
		    long bucket = mv.getMillis() / seconds;     
		    if( current != 0 && bucket != current ) {
		    	long millis = current * seconds; 
		        Metric result = closure.run( new ListBackedMetricRange(null,millis,millis+seconds*1000,block) );
			    if( LOGGER.isTraceEnabled() ) { 
			    	Date dt = new Date( millis );
			        LOGGER.trace( "collate: processed bucket: " + dt + "(" + millis + "), result: " + result + ", block: " + block ); 
			    }
		        if( result != null ) {
		          ret.add( result );  
		        }
		        block.clear();
		    }
			current = bucket;
		    block.add( mv );
		}
		// If any leftover in last chunk
		if( block.size() > 0 ) {
			long millis = current * seconds; 
		    Metric result = closure.run( new ListBackedMetricRange(null,millis,millis+seconds*1000,block)  );
		    if( LOGGER.isTraceEnabled() ) { 
		    	Date dt = new Date( millis );
		    	LOGGER.trace( "collate: processed bucket: " + dt + "(" + millis + "), result: " + result + ", block: " + block ); 
		    }
			if( result != null ) {
				ret.add( result );  
			}
		}
		if( ret.size() > 0 ) {
			return new ListBackedMetricRange(description, ret.get(0).getMillis(),ret.get(ret.size()-1).getMillis(), ret); 
		}
		return null;
	}
}

package org.scriptbox.metrics.compute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.CollatingIterator;
import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.metrics.query.main.MetricException;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricCollator {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricCollator.class );
	
	/**
	 * A safeguard against possible accidental overrun of date ranges
	 */
	private static int MAX_METRICS = 50000; 
	
	private String name;
	private String id;
	private int seconds;
	private Collection<? extends MetricRange> ranges;

	private long first;
	private long last;
	private long start;
	private long end;
	private DateRange dates;
	
	public MetricCollator( String name, String id, int seconds, Collection<? extends MetricRange> ranges ) {
		this.name = name;
		this.id = id;
		this.seconds = seconds;
		this.ranges = ranges;
		
	    first = Long.MAX_VALUE;
	    last = Long.MIN_VALUE;
	    start = Long.MAX_VALUE;
	    end = Long.MIN_VALUE;
	     
	    for( MetricRange range : ranges ) {
	    	DateRange dr = range.getFullDateRange();
	    	first = Math.min(first, dr.getStart().getTime());
	    	last = Math.max(last, dr.getEnd().getTime());
	    	start = Math.min(start, range.getStart() );
	    	end = Math.max(end, range.getEnd() );
	    }
	    start = Math.max(start, first);
	    end = Math.min(end, last);
	    dates = new DateRange( new Date(first), new Date(last) ); 
	}

	public void multi( final ParameterizedRunnable<MultiMetric> closure ) throws Exception {
		
	    final float[] totals = new float[ ranges.size() ];
	    final int[] counts = new int[ ranges.size() ];
	    
	    collate( true, new ParameterizedRunnableWithResult<Metric,MetricRange>() {
	    	public Metric run( MetricRange range ) throws Exception {
	    		Iterator<MetricWithAssociation<Integer>> iter = (Iterator)range.getIterator(seconds);
    			while( iter.hasNext() ) {
    				MetricWithAssociation<Integer> metric = iter.next();
    				//
	              	// Filter each metric back into individual collections for each supplied list of metrics.
	              	// This is done by using the MetricValue.reference which was assigned an incrementing
	              	// number above.
	              	//
    				int ind = metric.getAssociate();
    				totals[ind] += metric.getValue();
    				counts[ind]++;
	          	}
    			float[] avg = new float[ totals.length ];
    			for( int i=0 ; i < totals.length ; i++ ) {
    				avg[i] = totals[i] / counts[i];
    			}
    			MultiMetric metric = new MultiMetric(range.getStart(), avg );
    			closure.run( metric );
    			Arrays.fill( totals, 0 );
    			Arrays.fill( counts, 0 );
         
    			return null;
	    	}
      	} );
	}
	
	public MetricRange group( final ParameterizedRunnableWithResult<Metric,List<MetricRange>> closure ) throws Exception {
		
	    // Preload collections to store values from the grouping. We try to reuse these temporary collections
	    // as much as possible instead of constantly allocating new ones since the given closure will not
	    // be retaining what we supply it.  
		final List<MetricRange> rangesByReferenceNumber = new ArrayList<MetricRange>(ranges.size());
	    final List<List<Metric>> metricsByReferenceNumber = new ArrayList<List<Metric>>(ranges.size());
	    
	    for( MetricRange range : ranges ) {
	    	List<Metric> metrics = new ArrayList<Metric>();    
	    	metricsByReferenceNumber.add( metrics );    
	    	rangesByReferenceNumber.add( new ListBackedMetricRange(
	    		range.getName(),range.getId(), range.getFullDateRange(),range.getStart(),range.getEnd(),metrics) );
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
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "collate: range[" + i + "]=" + range ); }
			Iterator<Metric> iter = range.getIterator( seconds );
			citer.addIterator( associate ? new MetricWithAssociationIterator<Integer>(i,iter) : iter );
			i++;
		}
		
		final List<Metric> block = new ArrayList<Metric>();
		final ListBackedMetricRange tmp = new ListBackedMetricRange(null,null,dates,0,0,block );
		long current = 0;
		while( citer.hasNext() ) {
			Metric mv = (Metric)citer.next();
		    long bucket = mv.getMillis() / seconds;     
		    if( current != 0 && bucket != current ) {
		    	call( closure, current, tmp, ret );
		    }
			current = bucket;
		    block.add( mv );
		    if( ret.size() > MAX_METRICS ) {
		    	throw new MetricException( "Exceeded maximum allowed metrics of: " + MAX_METRICS );
		    }
		}
		// If any leftover in last chunk
		if( block.size() > 0 ) {
			call( closure, current, tmp, ret );
		}
		if( ret.size() > 0 ) {
			return new ListBackedMetricRange(name, id, dates, start, end, ret); 
		}
		return null;
	}
	
	private void call( ParameterizedRunnableWithResult<Metric,MetricRange> closure, long current, ListBackedMetricRange range, List<Metric> ret ) throws Exception {
    	range.setStart( current * seconds ); 
    	range.setEnd( range.getStart() + seconds * 1000 );
        Metric result = closure.run( range );
	    if( LOGGER.isTraceEnabled() ) { 
	    	Date start = new Date( range.getStart() );
	    	Date end = new Date( range.getEnd() );
	        LOGGER.trace( "call: processed bucket: " + start + "(" + range.getStart() + ") - " + end + "(" + range.getEnd() + "), result: " + result + ", block: " + range.getMetrics() ); 
	    }
        range.getMetrics().clear();
		if( result != null ) {
			ret.add( result );  
		}

	}
}

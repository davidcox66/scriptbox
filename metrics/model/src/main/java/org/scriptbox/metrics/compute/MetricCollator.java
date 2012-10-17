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
	
	public MetricRange group( final ParameterizedRunnableWithResult<Metric,List<? extends MetricRange>> closure ) throws Exception {
		
	    // Preload collections to store values from the grouping. We try to reuse these temporary collections
	    // as much as possible instead of constantly allocating new ones since the given closure will not
	    // be retaining what we supply it.  
		final List<ListBackedMetricRange> rangesByReferenceNumber = new ArrayList<ListBackedMetricRange>(ranges.size());
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
    			for( ListBackedMetricRange lb : rangesByReferenceNumber ) {
    				lb.setStart( range.getStart() );
    				lb.setEnd( range.getEnd() );
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
		    	long bstart = current * seconds; 
		    	long bend = bstart + seconds*1000;
		    	tmp.setStart( bstart );
		    	tmp.setEnd( bend );
		        Metric result = closure.run( tmp );
			    if( LOGGER.isTraceEnabled() ) { 
			    	Date dt = new Date( bstart );
			        LOGGER.trace( "collate: processed bucket: " + dt + "(" + bstart + "), result: " + result + ", block: " + block ); 
			    }
		        if( result != null ) {
		        	if( result.getValue() == Float.NaN ) {
		        		throw new MetricException( "Computation generated an invalid number for metrics: " + block );
		        	}
		        	ret.add( result );  
		        }
		        block.clear();
		    }
			current = bucket;
		    block.add( mv );
		}
		// If any leftover in last chunk
		if( block.size() > 0 ) {
			long begin = current * seconds; 
			long end = begin + seconds*1000;
		    Metric result = closure.run( new ListBackedMetricRange(null,null,dates,begin,end,block)  );
		    if( LOGGER.isTraceEnabled() ) { 
		    	Date dt = new Date( begin );
		    	LOGGER.trace( "collate: processed bucket: " + dt + "(" + begin + "), result: " + result + ", block: " + block ); 
		    }
			if( result != null ) {
	        	if( result.getValue() == Float.NaN ) {
	        		throw new MetricException( "Computation generated an invalid number for metrics: " + block );
	        	}
				ret.add( result );  
			}
		}
		if( ret.size() > 0 ) {
			return new ListBackedMetricRange(name, id, dates, start, end, ret); 
		}
		return null;
	}
}

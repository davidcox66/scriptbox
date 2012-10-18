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

/**
 * This walks through multiple MetricRanges simultaneously in specified intervals of time, processing all of the
 * metrics in each of these blocks as units, applying various computations on these blocks of metrics. All of the
 * reporting functionality is dependent upon this as most, if not all, function require working in these units.
 * More simple examples of this would be min, max, average which calculate their respective values on these 
 * chunks of values. 
 * 
 * This functionality is highly dependent on the contract of the MetricRange which guarantees metrics will 
 * be retrieved at the specified resolution or better. Failing to do so would skew results as values would be missing
 * in calculations.
 * 
 * @author david
 */
public class MetricCollator {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricCollator.class );

	/**
	 * Used by the common-lang ColattingIterator to retrieve Metrics in order by time
	 */
	private static final Comparator<Metric> metricComparator = new Comparator<Metric>() {
		public int compare( Metric mv1, Metric mv2 ) {
	        return (int)(mv1.getMillis() - mv2.getMillis());
		}
	};
	
	/**
	 * Used by the common-lang ColattingIterator to retrieve MetricsWithAssociation in order by time
	 */
	private static final Comparator<MetricWithAssociation<Integer>> metricWithAssociationComparator = new Comparator<MetricWithAssociation<Integer>>() {
		public int compare( MetricWithAssociation<Integer> mv1, MetricWithAssociation<Integer> mv2 ) {
	        return (int)(mv1.getMetric().getMillis() - mv2.getMetric().getMillis());
		}
	};
	
	interface MetricAdapter {
		public long getMillis( Object obj );
	}
	
	private static MetricAdapter metricAdapter = new MetricAdapter() {
		public long getMillis( Object obj ) {
			return ((Metric)obj).getMillis();
		}
	};
	
	private static MetricAdapter metricWithAssociationAdapter = new MetricAdapter() {
		public long getMillis( Object obj ) {
			return ((MetricWithAssociation<Integer>)obj).getMetric().getMillis();
		}
	};
	
	/**
	 * A safeguard against possible accidental overrun of date ranges. If you've gotten this far
	 * then you are probably going to keep going until you run out of memory.
	 */
	private static int MAX_METRICS = 50000; 
	
	/**
	 * A name to assign to computed MetricRanges
	 */
	private String name;
	
	/**
	 * An id to assign the computed MetricRanges
	 */
	private String id;
	
	/**
	 * The block of time in which metrics are collated 
	 */
	private int seconds;
	
	private Collection<? extends MetricRange> ranges;

	/**
	 * The earliest first time of the all the ranges
	 */
	private long first;
	
	/** 
	 * The latest last time of all the ranges
	 */
	private long last;
	
	/**
	 * The earliest start time of all the ranges
	 */
	private long start;
	
	/** 
	 * The latest end time of all the ranges
	 */
	private long end;
	
	/**
	 * A date range of the earliest first time and latest last time
	 */
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
	    
	    if( first == Long.MIN_VALUE ) {
	    	throw new IllegalArgumentException( "First cannot be Long.MIN_VALUE" );
	    }
	    if( start == Long.MIN_VALUE ) {
	    	throw new IllegalArgumentException( "Start cannot be Long.MIN_VALUE" );
	    }
	    if( end == Long.MAX_VALUE ) {
	    	throw new IllegalArgumentException( "End cannot be Long.MAX_VALUE" );
	    }
	    if( last == Long.MAX_VALUE ) {
	    	throw new IllegalArgumentException( "Last cannot be Long.MIN_VALUE" );
	    }
	    
	    dates = new DateRange( new Date(first), new Date(last) ); 
	    if( LOGGER.isDebugEnabled() ) {
	    	LOGGER.debug( "MetricCollator: name=" + name + ", id=" + id + ", seconds=" + seconds + ", ranges=" + ranges +
	    	    ", start=" + start + ", end=" + end + ", dates=" + dates );
	    }
	}

	/**
	 * This walks a collection of MetricRanges, converting each block of Metrics into a single MultiMetric with the average
	 * of values. This is required for GXT multi-series charts as their models are a list of objects where each object
	 * contains a value for each series. Unlike the other methods in MetricCollector, this does no retain or return
	 * the computed values. Instead the given closure is responsible for collecting the data in whatever form it
	 * chooses.
	 *  
	 * @param closure
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void multi( final ParameterizedRunnable<MultiMetric> closure ) throws Exception {
		
	    final float[] totals = new float[ ranges.size() ];
	    final int[] counts = new int[ ranges.size() ];
	    
	    collate( true, new ParameterizedRunnableWithResult<Metric,MetricRange>() {
			public Metric run( MetricRange range ) throws Exception {
	    		Iterator<MetricWithAssociation<Integer>> iter = (Iterator)range.getIterator(seconds);
    			while( iter.hasNext() ) {
    				MetricWithAssociation<Integer> ma = iter.next();
    				int ind = ma.getAssociate();
    				totals[ind] += ma.getMetric().getValue();
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
	
	/**
	 * This builds upon the functionality of the collate() method which walks through multiple MetricRanges in chunks of time.
	 * Whereas collate() does not differentiate between which MetricRange each originate from, this retains that 
	 * association and keeps the collated metric for each MetricRange together. This allows for computation that compare
	 * and contract the MetricRanges with one another.
	 * 
	 * @param closure
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetricRange group( final ParameterizedRunnableWithResult<Metric,MetricRange[]> closure ) throws Exception {
		
	    // Preload collections to store values from the grouping. We try to reuse these temporary collections
	    // as much as possible instead of constantly allocating new ones since the given closure will not
	    // be retaining what we supply it.  
		final ListBackedMetricRange[] rangesByReferenceNumber = new ListBackedMetricRange[ranges.size()];
	    final List[] metricsByReferenceNumber = new List[ranges.size()];
	    
	    int i=0;
	    for( MetricRange range : ranges ) {
	    	List<Metric> metrics = new ArrayList<Metric>();    
	    	metricsByReferenceNumber[i] = metrics;    
	    	rangesByReferenceNumber[i] = new ListBackedMetricRange(
	    		range.getName(),range.getId(), range.getFullDateRange(),0,0,metrics);
	    	i++;
	    }
	     
	    return collate( true, new ParameterizedRunnableWithResult<Metric,MetricRange>() {
			public Metric run( MetricRange range ) throws Exception {
	    		Iterator<MetricWithAssociation<Integer>> iter = (Iterator)range.getIterator(seconds);
    			while( iter.hasNext() ) {
    				MetricWithAssociation<Integer> ma = iter.next();
		          	metricsByReferenceNumber[ma.getAssociate()].add( ma.getMetric() );
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

	/**
	 * Walks through all of the supplied MetricRanges in chunks of time in the specified number of seconds. This is the
	 * building block upon which much of the reporting functionality works. Multiple MetricRanges are subdivided by time
	 * and computations are performed on these blocks
	 * @param associate
	 * @param closure
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MetricRange collate( boolean associate, ParameterizedRunnableWithResult<Metric,MetricRange> closure ) throws Exception {

		int chunk = seconds * 1000;
		final List<Metric> ret = new ArrayList<Metric>( (int)(end - start) / chunk );
		CollatingIterator citer = new CollatingIterator( associate ? metricWithAssociationComparator : metricComparator ); 
		MetricAdapter adapter = associate ? metricWithAssociationAdapter : metricAdapter;		
		
		int i=0;
		for( MetricRange range : ranges ) {
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "collate: range[" + i + "]=" + range + ", metrics=" + range.getMetrics(chunk) ); }
			Iterator<Metric> iter = range.getIterator( seconds );
			citer.addIterator( associate ? new MetricWithAssociationIterator<Integer>(i,iter) : iter );
			i++;
		}
		
		final List block = new ArrayList();
		final ListBackedMetricRange tmp = new ListBackedMetricRange(null,null,dates,0,0,block );
		long current = 0;
		while( citer.hasNext() ) {
			Object mv = citer.next();
		    long bucket = adapter.getMillis(mv) / chunk;     
		    if( current != 0 && bucket != current ) {
		    	call( closure, current, chunk, tmp, ret );
		    }
			current = bucket;
		    block.add( mv );
		    if( ret.size() > MAX_METRICS ) {
		    	throw new MetricException( "Exceeded maximum allowed metrics of: " + MAX_METRICS );
		    }
		}
		// If any leftover in last chunk
		if( block.size() > 0 ) {
			call( closure, current, chunk, tmp, ret );
		}
		ListBackedMetricRange rng = ret.size() > 0 ?  new ListBackedMetricRange(name, id, dates, start, end, ret) : null; 
		if( LOGGER.isTraceEnabled() ) {
			LOGGER.trace( "collate: range: " + rng  + ", metrics: " + (rng != null ? rng.getMetrics() : null) );
		}
		return rng;
	}
	
	private void call( ParameterizedRunnableWithResult<Metric,MetricRange> closure, long bucket, long chunk, ListBackedMetricRange range, List<Metric> ret ) throws Exception {
    	range.setStart( bucket * chunk ); 
    	range.setEnd( range.getStart() + chunk - 1 );
        Metric result = closure.run( range );
	    if( LOGGER.isTraceEnabled() ) { 
	    	Date start = new Date( range.getStart() );
	    	Date end = new Date( range.getEnd() );
	        LOGGER.trace( "call: processed bucket: " + start + "(" + range.getStart() + ") - " + end + "(" + range.getEnd() + "), " +
	        	"result: " + result + ", block: " + range.getMetrics() ); 
	    }
        range.getMetrics().clear();
		if( result != null ) {
			ret.add( result );  
		}

	}
}

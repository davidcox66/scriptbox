package org.scriptbox.metrics.model;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.query.main.MetricProvider;

/**
 * An abstraction which represents a subset of metrics within a specified time range. This is the
 * primary means in which most of the metric APIs work with values in mass.
 * 
 * @author david
 */
public abstract class MetricRange implements MetricProvider {

	protected long start;
	protected long end;
	
	public MetricRange( long start, long end ) {
		this.start = start;
		this.end = end;
	}

	/** 
	 * Gets an value which uniquely identifies this range within the system. This look likely be the id
	 * of the corresponding metric tree node.
	 */
	public abstract String getId(); 
	
	/**
	 * A displayable value for this range
	 */
	public abstract String getName();

	
	/**
	 * An iterator which will walk through the underlying sequence of metric in time order. This iterator
	 * should guarantee that the metrics occur at least once in each specified interval (seconds). This is
	 * important as various reporting functions work in chunks of time where absent values would skew results.
	 * 
	 * @param resolution
	 * @return
	 */
	public abstract Iterator<Metric> getIterator( final int resolution );
	
	/**
	 * Most interaction with individual metrics works in term of the iterator, though occasionally 
	 * a list is appropriate. A client could iterate over the values to collect them but
	 * an implementation of the MetricRange which is aware of the underlying storage may be
	 * able to do so more efficiently.
	 * 
	 * @param resolution
	 * @return
	 */
	public abstract List<Metric> getMetrics( int resolution ); 
	
	public Date getStartDate() {
		return new Date( start );
	}

	public long getStart() {
		return start;
	}

	public Date getEndDate() {
		return new Date( end );
	}
	
	public long getEnd() {
		return end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + (int) (start ^ (start >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetricRange other = (MetricRange) obj;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	
}

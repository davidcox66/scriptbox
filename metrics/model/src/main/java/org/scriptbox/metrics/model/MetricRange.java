package org.scriptbox.metrics.model;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

public abstract class MetricRange {

	private long start;
	private long end;
	
	public MetricRange( long start, long end ) {
		this.start = start;
		this.end = end;
	}

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

	public abstract List<Metric> getMetrics( int resolution ); 
	public abstract Iterator<Metric> getIterator( final int resolution ); 

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

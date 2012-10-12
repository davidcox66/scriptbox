package org.scriptbox.metrics.model;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.query.main.MetricProvider;

public abstract class MetricRange implements MetricProvider {

	private String description;
	private long start;
	private long end;
	
	public MetricRange( String description,  long start, long end ) {
		this.description = description;
		this.start = start;
		this.end = end;
	}

	public String getDescription() {
		return description;
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

	// public abstract boolean isTransient();
	public abstract List<Metric> getMetrics( int resolution ); 
	public abstract Iterator<Metric> getIterator( final int resolution );

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
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
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (end != other.end)
			return false;
		if (start != other.start)
			return false;
		return true;
	} 


	
}

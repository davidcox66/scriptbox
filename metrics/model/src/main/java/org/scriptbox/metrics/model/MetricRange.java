package org.scriptbox.metrics.model;

import java.util.Iterator;
import java.util.List;

public abstract class MetricRange {

	private long start;
	private long end;
	
	public MetricRange( long start, long end ) {
		this.start = start;
		this.end = end;
	}


	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public abstract MetricResolution getResolution(); 
	public abstract MetricSequence getSequence(); 
	public abstract List<Metric> getMetrics();
	
	public Iterator<Metric> getConstantResolutionIterator() {
		final List<Metric> metrics = getMetrics();
		return new Iterator<Metric>() {
			long cstart = start;
			MetricResolution res = getResolution(); 
			Metric last = null;
			Metric pending = null;
			Iterator<Metric> iter = metrics.iterator();
			public boolean hasNext() {
				return iter.hasNext() || pending != null;
			}
			public Metric next() {
				Metric ret = null;
				long cend = cstart + res.getMillis();
				if( last == null ) {
					pending = last = iter.next();
				}
				if( pending == null ) {
					pending = iter.next();
				}
				if( pending.isBetween(cstart, cend) ) {
					ret = pending;
					pending = null;
				}
				else {
					ret = new Metric(cstart, last.getValue() );
				}
				cstart += ret.getMillis();
				return ret;
			}
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported");
			}
		};
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

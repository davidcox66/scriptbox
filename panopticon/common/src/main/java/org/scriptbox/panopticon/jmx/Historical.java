package org.scriptbox.panopticon.jmx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Date;

public class Historical<X> {

	private int maxSize;
	private int maxMillis;
	private LinkedList<Sample<X>> samples = new LinkedList<Sample<X>>();
	
	public static class Sample<X> {
		long millis;
		X data;
		
		public Sample( X data ) {
			millis = System.currentTimeMillis();
			this.data = data;
		}
		
		public Date getTime() {
			return new Date(millis);
		}
		
		public long getMillis() {
			return millis;
		}
		
		public X getData() {
			return data;
		}
	}
	
	public Historical( int maxSize, int maxSeconds ) {
		if( maxSize < 0 ) {
			throw new IllegalArgumentException( "maxSize cannot be less than zero");
		}
		if( maxSeconds < 0 ) {
			throw new IllegalArgumentException( "maxSeconds cannot be less than zero");
		}
		if( maxSize == 0 && maxSeconds == 0 ) {
			throw new IllegalArgumentException( "maxSize and maxSeconds cannot both be zero");
		}
		this.maxSize = maxSize;
		this.maxMillis = maxSeconds * 1000;
	}

	public void add( X data ) {
		samples.addFirst( new Sample<X>(data) );
		if( maxSize > 0 ) {
			while( samples.size() > maxSize ) {
				samples.removeLast();
			}
		}
		if( maxMillis > 0 ) {
			Iterator<Sample<X>> iter = samples.descendingIterator();
			long now = System.currentTimeMillis();
			while( iter.hasNext() ) {
				Sample<X> sample = iter.next();
				if( sample.getMillis() < now - maxMillis ) {
					iter.remove();
				}
			}
		}
	}
	
	public Sample<X> getLatest() {
		return size() > 0 ? samples.getFirst() : null;
	}
	
	public Sample<X> getPrevious() {
		return size() > 1 ? samples.get(1) : null;
	}
	
	public Sample<X> getOldest() {
		return size() > 1 ? samples.getLast() : null;
	}
	
	public List<Sample<X>> getAll() {
		return samples;
	}
	
	public List<Sample<X>> getAllWithin( int seconds ) {
		long oldest = System.currentTimeMillis() - (seconds*1000);
		List<Sample<X>> ret = new ArrayList<Sample<X>>();
		for( Sample<X> sample : samples ) {
			if( sample.getMillis() < oldest ) {
				break;
			}
			ret.add( sample );
		}
		return ret;
	}
	
	public int size() {
		return samples.size();
	}
	
}

package org.scriptbox.metrics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MetricResolution implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int seconds;
	private long ttl;
	
	public static List<MetricResolution> create( int... seconds ) {
		List<MetricResolution> ret = new ArrayList<MetricResolution>();
		for( int second : seconds ) {
			ret.add( new MetricResolution(second) );
		}
		return ret;
	}
	
	public MetricResolution( int seconds ) {
		this( seconds, 0 );
	}
	
	public MetricResolution( int seconds, long ttl ) {
		this.seconds = seconds;
		this.ttl = ttl;
	}

	public int getSeconds() {
		return seconds;
	}

	public long getTtl() {
		return ttl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + seconds;
		result = prime * result + (int) (ttl ^ (ttl >>> 32));
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
		MetricResolution other = (MetricResolution) obj;
		if (seconds != other.seconds)
			return false;
		if (ttl != other.ttl)
			return false;
		return true;
	}
	
}

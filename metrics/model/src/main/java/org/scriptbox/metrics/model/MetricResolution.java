package org.scriptbox.metrics.model;

import java.io.Serializable;

public class MetricResolution implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int millis;
	private long ttl;
	
	public MetricResolution( int millis, long ttl ) {
		this.millis = millis;
		this.ttl = ttl;
	}

	public int getMillis() {
		return millis;
	}

	public void setMillis(int millis) {
		this.millis = millis;
	}

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + millis;
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
		if (millis != other.millis)
			return false;
		if (ttl != other.ttl)
			return false;
		return true;
	}
	
	
}

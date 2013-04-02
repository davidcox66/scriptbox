package org.scriptbox.panopticon.apache;

public class ApacheLogTotal {

	public String url;
	public long min = Integer.MAX_VALUE;
	public long max = Integer.MIN_VALUE;
	public int totalRequests; 
	public long totalResponseTime; 

	public ApacheLogTotal( String url ) {
		this.url = url;
	}
	
	void reset() {
		min = Integer.MAX_VALUE;
		max = Integer.MIN_VALUE;
		totalRequests=0; 
		totalResponseTime=0;
	}
}

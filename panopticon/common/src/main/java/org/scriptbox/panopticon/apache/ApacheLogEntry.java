package org.scriptbox.panopticon.apache;

public class ApacheLogEntry {

	public String url;
	public long responseTime;
	    
    public ApacheLogEntry( String url, long responseTime ) {
        this.url = url;
        this.responseTime = responseTime;
    }
    
    public String toString() {
        return "ApacheLogEntry{ url=" + url + ", responseTime=" + responseTime + " }";
    }
}

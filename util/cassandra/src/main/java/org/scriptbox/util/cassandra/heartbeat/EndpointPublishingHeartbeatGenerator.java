package org.scriptbox.util.cassandra.heartbeat;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class EndpointPublishingHeartbeatGenerator extends
		CassandraHeartbeatGenerator {

	private String host;
	private int port;
	private List<String> tags;
	
	public EndpointPublishingHeartbeatGenerator() {
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public void start() {
		if( StringUtils.isEmpty(host) ) {
			throw new IllegalArgumentException( "Host cannot be null" );
		}
		if( port == 0 ) {
			throw new IllegalArgumentException( "Port must be specified" );
		}
		if( tags == null || tags.size() == 0  ) {
			throw new IllegalArgumentException( "Tags cannot be empty" );
		}
		super.start();
	}
	
	@Override
	public void populate(Heartbeat beat) {
		StringBuilder builder = new StringBuilder( 64 );
		for( String tag : tags ) {
			if( builder.length() > 0 ) {
				builder.append( "," );
			}
			builder.append( tag );
		}
		Map<String,String> params = beat.getParameters();
		params.put( "host", host );
		params.put( "port", ""+port );
		params.put( "tags", builder.toString() );
	}

}

package org.scriptbox.util.cassandra.heartbeat;


public class EndpointHeartbeatGenerator<X extends Endpoint> extends
		CassandraHeartbeatGenerator<X> {

	private X endpoint;
	
	public EndpointHeartbeatGenerator() {
	}

	public X getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(X endpoint) {
		this.endpoint = endpoint;
	}

	public void start() {
		if( endpoint == null ) {
			throw new IllegalArgumentException( "Endpoint cannot be null" );
		}
		super.start();
	}
	
	@Override
	public X data() {
		return endpoint;
	}

}

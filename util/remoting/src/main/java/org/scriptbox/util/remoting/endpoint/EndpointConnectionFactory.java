package org.scriptbox.util.remoting.endpoint;



public interface EndpointConnectionFactory<X> {

	public Connection<X> create( Endpoint endpoint ) throws Exception;
}

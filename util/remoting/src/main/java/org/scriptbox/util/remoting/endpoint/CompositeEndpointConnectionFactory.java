package org.scriptbox.util.remoting.endpoint;

import java.util.ArrayList;
import java.util.List;

public class CompositeEndpointConnectionFactory<X> implements EndpointConnectionFactory<X> {

	private List<EndpointConnectionFactory<X>> factories = new ArrayList<EndpointConnectionFactory<X>>();
	
	public Connection<X> create( Endpoint endpoint ) throws Exception {
		for( EndpointConnectionFactory<X> factory : factories ) {
			Connection<X> conn = factory.create( endpoint );
			if( conn != null ) {
				return conn;
			}
		}
		return null;
	}
}

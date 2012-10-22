package org.scriptbox.util.remoting.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

public class CompositeEndpointConnectionFactory<X> implements EndpointConnectionFactory<X>, InitializingBean {

	private List<EndpointConnectionFactory<X>> factories = new ArrayList<EndpointConnectionFactory<X>>();

	public void add( EndpointConnectionFactory<X> factory ) {
		factories.add( factory );
	}
	
	public List<EndpointConnectionFactory<X>> getFactories() {
		return factories;
	}

	public void setFactories(List<EndpointConnectionFactory<X>> factories) {
		this.factories = factories;
	}

	public void afterPropertiesSet() throws Exception {
		if( factories == null || factories.size() == 0 ) {
			throw new IllegalArgumentException( "Must specify at least one factory");
		}
	}
	
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

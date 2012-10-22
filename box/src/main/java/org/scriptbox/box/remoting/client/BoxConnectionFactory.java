package org.scriptbox.box.remoting.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.box.remoting.server.BoxInterface;
import org.scriptbox.util.remoting.endpoint.Connection;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.scriptbox.util.remoting.endpoint.EndpointConnectionFactory;
import org.springframework.beans.factory.InitializingBean;

public abstract class BoxConnectionFactory implements EndpointConnectionFactory<BoxInterface>, InitializingBean {

	private Map<Endpoint,Connection<BoxInterface>> connections = Collections.synchronizedMap(new HashMap<Endpoint,Connection<BoxInterface>>());
	
	private String format;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void afterPropertiesSet() throws Exception {
		if( StringUtils.isEmpty(format) ) {
			throw new IllegalArgumentException( "Format must be specified");
		}
	}
	
	public Connection<BoxInterface> create( Endpoint endpoint ) throws Exception {
		Connection<BoxInterface> conn = connections.get( endpoint );
		if( conn == null ) {
			conn = build( endpoint );
			if( conn != null ) {
				connections.put( endpoint, conn );
			}
		}
		return conn;
	}
	
	public void close( Connection<BoxInterface> conn ) {
		connections.remove( conn );
	}
	
	protected abstract Connection<BoxInterface> build( Endpoint endpoint ) throws Exception;
}

package org.scriptbox.box.remoting.client;

import org.scriptbox.box.remoting.server.BoxInterface;
import org.scriptbox.util.remoting.endpoint.Connection;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.scriptbox.util.remoting.endpoint.TcpEndpoint;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class BoxDirectConnectionFactory extends BoxConnectionFactory {
	
	protected Connection<BoxInterface> build( final Endpoint endpoint ) throws Exception {
		if( endpoint instanceof TcpEndpoint ) {
			TcpEndpoint tcp = (TcpEndpoint)endpoint;
			HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
			
			String user = System.getProperty( "spring.user" );
			if( user != null ) {
				factory.setUsername( user );
				factory.setPassword( System.getProperty("spring.password") );
			}
			
			factory.setServiceUrl(getFormat().replaceAll("%host%", tcp.getHost()).replaceAll("%port%",""+tcp.getPort()));
			factory.setServiceInterface(BoxInterface.class);
			factory.afterPropertiesSet();
			final BoxInterface box = (BoxInterface)factory.getObject();
			return new Connection<BoxInterface>() {
				public BoxInterface getProxy() {
					return box;
				}
				public Endpoint getEndpoint() {
					return endpoint;
				}
				public String getIdentifier() {
					return endpoint.getIdentifier();
				}
				public void close() {
					BoxDirectConnectionFactory.this.close( this );
				}
			};
		}
		return null;
	}
}

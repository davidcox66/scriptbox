package org.scriptbox.box.plugins.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxConnection {

	private JMXServiceURL url;
	private JMXConnector connector;
	private MBeanServerConnection server;
	private Map<String,String[]> environment;
	
	public JmxConnection( String url ) throws MalformedURLException {
        this.url = new JMXServiceURL( url );
	}
	public JmxConnection( String host, int port ) throws MalformedURLException {
       url = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi" );
	}
	
	public JmxConnection( String host, int port, String user, String password ) throws MalformedURLException {
	   this( host, port );
	   environment = new HashMap<String, String[]>();
	   environment.put(JMXConnector.CREDENTIALS, new String[] {user, password} );
	}
	
	public JMXServiceURL getUrl() {
		return url;
	}
	
	public JMXConnector getConnector() throws IOException {
		if( connector == null ) {
			if( environment != null ) {
				connector = JMXConnectorFactory.connect( url, environment );
			}
			else {
				connector = JMXConnectorFactory.connect( url ); 
			}
		}
		return connector;
	}

	public MBeanServerConnection getServer() throws IOException {
	    if( server == null ) {
	    	server = getConnector().getMBeanServerConnection();
	    }
	    return server;
	}

	public String toString() {
		return "JmxConnection { " + url + " }";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		JmxConnection other = (JmxConnection) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
}

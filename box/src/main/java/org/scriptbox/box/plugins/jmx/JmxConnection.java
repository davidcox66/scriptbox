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
}

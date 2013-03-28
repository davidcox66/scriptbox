package org.scriptbox.box.jmx.conn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxRmiConnection extends JmxConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger( JmxRmiConnection.class ) ;
	
	private JMXServiceURL url;
	private JMXConnector connector;
	private MBeanServerConnection server;
	private Map<String,String[]> environment;
	
	public JmxRmiConnection( String url ) throws MalformedURLException {
        this.url = new JMXServiceURL( url );
	}
	public JmxRmiConnection( String host, int port ) throws MalformedURLException {
       String str = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
       if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "Connecting to JMX url: '" + str + "'"); }
       url = new JMXServiceURL( str );
       
	}
	
	public JmxRmiConnection( String host, int port, String user, String password ) throws MalformedURLException {
	   this( host, port );
	   environment = new HashMap<String, String[]>();
       if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "Connecting to JMX host/port: '" + host + "/" + port + "'"); }
	   environment.put(JMXConnector.CREDENTIALS, new String[] {user, password} );
	}

	public AttributeList getAttributes( ObjectName objectName, Collection attributes ) 
		throws IOException, ReflectionException, InstanceNotFoundException 
	{
		String[] namesArray = new String[attributes.size()];
		attributes.toArray(namesArray);
		return getServer().getAttributes(objectName, namesArray);
	}
	
	public MBeanAttributeInfo[] getAttributeInfo( ObjectName objectName ) 
		throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException 
	{
		return getServer().getMBeanInfo(objectName).getAttributes();
	}
	public Set<ObjectName> queryNames( ObjectName objectName, QueryExp expression ) 
		throws IOException 
	{
		return getServer().queryNames( objectName, expression );
	}
	
	public Object invoke( ObjectName objectName, String methodName, Object[] arguments, String[] signature ) 
		throws InstanceNotFoundException, MBeanException, ReflectionException, IOException 
	{
		return getServer().invoke( objectName, methodName, arguments, signature );
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
		JmxRmiConnection other = (JmxRmiConnection) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
}

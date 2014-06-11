package org.scriptbox.box.jmx.conn;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;
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

	public Object getAttribute( ObjectName objectName, String attribute ) throws Exception
	{
		try {
			return getServer().getAttribute(objectName, attribute);
		}
		catch( Exception ex ) {
			reset(ex);
			throw ex;
		}
		
	}
	
	public AttributeList getAttributes( ObjectName objectName, Collection attributes ) throws Exception {
		try {
			String[] namesArray = new String[attributes.size()];
			attributes.toArray(namesArray);
			return getServer().getAttributes(objectName, namesArray);
		}
		catch( Exception ex ) {
			reset(ex);
			throw ex;
		}
	}
	
	public MBeanAttributeInfo[] getAttributeInfo( ObjectName objectName ) throws Exception {
		try {
			return getServer().getMBeanInfo(objectName).getAttributes();
		}
		catch( Exception ex ) {
			reset(ex);
			throw ex;
		}
	}
	
	public Set<ObjectName> queryNames( ObjectName objectName, QueryExp expression ) throws Exception {
		try {
			return getServer().queryNames( objectName, expression );
		}
		catch( Exception ex ) {
			reset(ex);
			throw ex;
		}
	}
	
	public Object invoke( ObjectName objectName, String methodName, Object[] arguments, String[] signature ) throws Exception {
		try {
			return getServer().invoke( objectName, methodName, arguments, signature );
		}
		catch( Exception ex ) {
			reset(ex);
			throw ex;
		}
			
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

	private void reset( Exception ex ) {
		if( isConnectException(ex) ) {
			reset();
		}
	}
	
	public void reset() {
		connector = null;
		server = null;
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
	
	
	public static boolean isConnectException( Exception ex ) {
		Throwable curr = ex;
		do {
			if( curr instanceof java.net.ConnectException || 
				curr instanceof java.rmi.ConnectException || 
				curr instanceof javax.naming.ServiceUnavailableException )
			{
				return true;
			}
			curr = ex.getCause();
		} while( curr != null );
		return false;
	}
}

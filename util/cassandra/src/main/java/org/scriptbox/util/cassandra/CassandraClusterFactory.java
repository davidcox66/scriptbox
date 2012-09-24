package org.scriptbox.util.cassandra;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to remove Cassandra configuration concerns from DAO objects. 
 * This is invoked via a spring factory method that allows injection of the
 * Hector Cluster object into the DAO. 
 * <p>
 *
 */
public class CassandraClusterFactory
{
	static final Logger LOGGER = LoggerFactory.getLogger( CassandraClusterFactory.class );
  
	private String name;
	private String host;
	private int port;

	private Cluster cluster;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	synchronized public Cluster getInstance() {
		if( cluster == null ) {
			LOGGER.debug( "getInstance: creating cluster name=" + name + ", host=" + host + ", port=" + port );
			CassandraHostConfigurator config = new CassandraHostConfigurator(host);
			config.setPort( port );
			cluster = HFactory.getOrCreateCluster( name, config );
		}
		return cluster;
	}
}

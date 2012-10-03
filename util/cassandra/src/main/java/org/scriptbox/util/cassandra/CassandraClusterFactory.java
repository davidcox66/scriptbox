package org.scriptbox.util.cassandra;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;

import org.scriptbox.util.remoting.tunnel.Tunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

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

	private Tunnel tunnel;
	
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

	
	public Tunnel getTunnel() {
		return tunnel;
	}

	public void setTunnel(Tunnel tunnel) {
		this.tunnel = tunnel;
	}

	synchronized public Cluster getInstance() throws Exception {
		if( cluster == null ) {
			String chost = host;
			int cport = port;
			if( tunnel != null && StringUtils.isNotEmpty(tunnel.getTunnelHost()) ) {
				chost = "localhost";
				cport = tunnel.connect();
			}
			LOGGER.debug( "getInstance: creating cluster name=" + name + ", host=" + host + ", port=" + port );
			CassandraHostConfigurator config = new CassandraHostConfigurator(chost);
			config.setPort( cport );
			cluster = HFactory.getOrCreateCluster( name, config );
		}
		return cluster;
	}
}

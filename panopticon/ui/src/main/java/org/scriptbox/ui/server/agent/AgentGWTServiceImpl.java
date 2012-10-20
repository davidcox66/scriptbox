package org.scriptbox.ui.server.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.ui.shared.agent.AgentGWTService;
import org.scriptbox.util.cassandra.heartbeat.CassandraHeartbeatReader;
import org.scriptbox.util.cassandra.heartbeat.Endpoint;
import org.scriptbox.util.cassandra.heartbeat.Heartbeat;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.springframework.beans.factory.InitializingBean;

public class AgentGWTServiceImpl implements AgentGWTService, InitializingBean {

	private Cluster cluster;
	private Keyspace keyspace;
	private String columnFamilyName = CassandraHeartbeatReader.HEARTBEATS_CF;
	
	private CassandraHeartbeatReader<Endpoint> reader;

	public Cluster getCluster() {
		return cluster;
	}
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	public Keyspace getKeyspace() {
		return keyspace;
	}
	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
	public String getColumnFamilyName() {
		return columnFamilyName;
	}
	public void setColumnFamilyName(String columnFamilyName) {
		this.columnFamilyName = columnFamilyName;
	}

	public void afterPropertiesSet() throws Exception {
		if( cluster == null ) {
			throw new IllegalArgumentException( "Cluster must be specified");
		}
		if( keyspace == null ) {
			throw new IllegalArgumentException( "Keyspace must be specified");
		}
		if( StringUtils.isEmpty(columnFamilyName) ) {
			throw new IllegalArgumentException( "Column family name must be specified");
		}
		reader = new CassandraHeartbeatReader<Endpoint>( cluster, keyspace, columnFamilyName, Endpoint.class );
	}

	public Map<String, List<Heartbeat<Endpoint>>> getAgentHeartbeats( List<String> groups) throws ServiceException {
		try {
			Map<String,List<Heartbeat<Endpoint>>> ret = new HashMap<String,List<Heartbeat<Endpoint>>>();
			for( String group : groups ) {
				List<Heartbeat<Endpoint>> beats = reader.list( group );
				ret.put( group, beats );
			}
			return ret;
		}
		catch( Exception ex ) {
			throw new ServiceException( "Failed getting agents", ex );
		}
	}
	
	public String createContext( List<Endpoint> endpoints, String language, String contextName ) 
		throws ServiceException 
	{
		
	}
	
	public String startContext( List<Endpoint> endpoints, String contextName ) 
		throws ServiceException
	{
	}
	
	public String stopContext( List<Endpoint> endpoints, String contextName ) 
		throws ServiceException
	{
	}
	
	public String shutdownContext( List<Endpoint> endpoints, String contextName ) 
		throws ServiceException
	{
	}

	public String shutdownAllContexts( List<Endpoint> endpoints ) 
		throws ServiceException
	{
	}
	
	public String loadScript( List<Endpoint> endpoints, String contextName, String scriptName, String fileName, List arguments ) 
		throws ServiceException
	{
	}
	
	public String startScript( List<Endpoint> endpoints, String language, String contextName, String scriptName, String fileName, List arguments ) 
		throws ServiceException
	{
	}
	
	public String status( List<Endpoint> endpoints ) 
		throws ServiceException
	{
		
	}
}

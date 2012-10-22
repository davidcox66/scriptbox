package org.scriptbox.ui.server.agent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.box.remoting.client.BoxAgentHelper;
import org.scriptbox.box.remoting.client.BoxDirectConnectionFactory;
import org.scriptbox.box.remoting.client.BoxTunnelConnectionFactory;
import org.scriptbox.box.remoting.client.PrintStreamFactory;
import org.scriptbox.box.remoting.server.BoxInterface;
import org.scriptbox.ui.shared.agent.AgentGWTService;
import org.scriptbox.util.cassandra.heartbeat.CassandraHeartbeatReader;
import org.scriptbox.util.cassandra.heartbeat.Heartbeat;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.scriptbox.util.remoting.endpoint.CompositeEndpointConnectionFactory;
import org.scriptbox.util.remoting.endpoint.Endpoint;
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
	
	public Map<Endpoint,String> createContext( final List<Endpoint> endpoints, final String language, final String contextName ) 
		throws ServiceException 
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.createContext( language, contextName );
			}
		} );
	}
	
	public Map<Endpoint,String> startContext( final List<Endpoint> endpoints, final String contextName, final List arguments ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.startContext( contextName, arguments );
			}
		} );
	}
	
	public Map<Endpoint,String> stopContext( final List<Endpoint> endpoints, final String contextName ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.stopContext( contextName );
			}
		} );
	}
	
	public Map<Endpoint,String> shutdownContext( final List<Endpoint> endpoints, final String contextName ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.shutdownContext( contextName );
			}
		} );
	}

	public Map<Endpoint,String> shutdownAllContexts( List<Endpoint> endpoints ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.shutdownAllContexts();
			}
		} );
	}
	
	public Map<Endpoint,String> loadScript( 
		final List<Endpoint> endpoints, 
		final String contextName, 
		final String scriptName, 
		final String fileName, 
		final List arguments ) 
			throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.loadScript( contextName, scriptName, fileName, arguments );
			}
		} );
	}
	
	public Map<Endpoint,String> startScript( 
		final List<Endpoint> endpoints, 
		final String language, 
		final String contextName, 
		final String scriptName, 
		final String fileName, 
		final List arguments ) 
			throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.startScript( language, contextName, scriptName, fileName, arguments );
			}
		} );
	}
	
	public Map<Endpoint,String> status( List<Endpoint> endpoints ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.status();
			}
		} );
		
	}

	private Map<Endpoint,String> runAgents( List<Endpoint> endpoints, ParameterizedRunnable<BoxAgentHelper> runnable ) throws ServiceException {
		try {
			Map<Endpoint,ByteArrayOutputStream> streams = createAgentStreams();
			BoxAgentHelper helper = createAgentHelper( endpoints, streams );
			runnable.run( helper );
			helper.join();
			return getAgentOutput( streams );
		}
		catch( Exception ex ) {
			throw new ServiceException( "Failed calling agents", ex );
		}
		
	}
	private Map<Endpoint,ByteArrayOutputStream> createAgentStreams() {
		return Collections.synchronizedMap( new HashMap<Endpoint,ByteArrayOutputStream>() );
	}

	private Map<Endpoint,String> getAgentOutput( Map<Endpoint,ByteArrayOutputStream> streams ) {
		Map<Endpoint,String> ret = new HashMap<Endpoint,String>();
		for( Map.Entry<Endpoint,ByteArrayOutputStream> entry : streams.entrySet() ) {
			ret.put( entry.getKey(), entry.getValue().toString() );
		}
		return ret;
	}
	private BoxAgentHelper createAgentHelper( final List<Endpoint> endpoints, final Map<Endpoint,ByteArrayOutputStream> streams ) {
		BoxAgentHelper agentHelper = new BoxAgentHelper();
		agentHelper.setStreamFactory( new PrintStreamFactory() {
			public PrintStream create( Endpoint endpoint ) {
				ByteArrayOutputStream bstream = new ByteArrayOutputStream();
				streams.put( endpoint, bstream );
				return new PrintStream( bstream );
			}
		} );
		CompositeEndpointConnectionFactory<BoxInterface> connectionFactory = new CompositeEndpointConnectionFactory<BoxInterface>();
		connectionFactory.add( new BoxDirectConnectionFactory() );
		connectionFactory.add( new BoxTunnelConnectionFactory() );
		agentHelper.setEndpointConnectionFactory( connectionFactory );
	
		agentHelper.setEndpoints( endpoints );
		
		return agentHelper;
	}
}

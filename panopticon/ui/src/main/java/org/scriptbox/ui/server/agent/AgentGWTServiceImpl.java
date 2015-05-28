package org.scriptbox.ui.server.agent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
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
import org.scriptbox.ui.shared.agent.Agent;
import org.scriptbox.ui.shared.agent.AgentGWTService;
import org.scriptbox.util.cassandra.heartbeat.CassandraHeartbeatReader;
import org.scriptbox.util.cassandra.heartbeat.Heartbeat;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.scriptbox.util.remoting.endpoint.CompositeEndpointConnectionFactory;
import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="agentService",scope=ServiceScope.SESSION)
public class AgentGWTServiceImpl implements AgentGWTService, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( AgentGWTServiceImpl.class );
	
	private Cluster cluster;
	private Keyspace keyspace;
	private String columnFamilyName = CassandraHeartbeatReader.HEARTBEATS_CF;
	
	private CassandraHeartbeatReader<Endpoint> reader;
	private Map<Agent,Heartbeat<Endpoint>> agentHeartbeatMapping;
	private Map<Endpoint,Agent> endpointAgentMapping;
	
	
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

	public List<Agent> getAgents( List<String> groups ) throws ServiceException {
		try {
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getAgents: groups=" + groups ); }
			agentHeartbeatMapping = new HashMap<Agent,Heartbeat<Endpoint>>();
			endpointAgentMapping = new HashMap<Endpoint,Agent>();
			List<Agent> ret = new ArrayList<Agent>();
			for( String group : groups ) {
				List<Heartbeat<Endpoint>> beats = reader.list( group );
				for( Heartbeat<Endpoint> heartbeat : beats ) {
					Agent agent = new Agent( heartbeat.getId(), heartbeat.getGroup(), heartbeat.getType(), heartbeat.getTags(), heartbeat.getData().getIdentifier() );
					agentHeartbeatMapping.put( agent, heartbeat );
					endpointAgentMapping.put( heartbeat.getData(), agent );
					ret.add( agent );
					if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getAgents: agent=" + agent ); }
				}
			}
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getAgents: agent.size()=" + ret.size() ); }
			return ret;
		}
		catch( Exception ex ) {
			throw new ServiceException( "Failed getting agents", ex );
		}
	}
	
	public Map<Agent,String> createContext( final List<Agent> endpoints, final String language, final String contextName ) 
		throws ServiceException 
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.createContext( language, contextName, new HashMap<String, Object>() );
			}
		} );
	}
	
	public Map<Agent,String> startContext( final List<Agent> endpoints, final String contextName, final List<String> arguments ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.startContext( contextName, arguments );
			}
		} );
	}
	
	public Map<Agent,String> stopContext( final List<Agent> endpoints, final String contextName ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.stopContext( contextName );
			}
		} );
	}
	
	public Map<Agent,String> shutdownContext( final List<Agent> endpoints, final String contextName ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.shutdownContext( contextName );
			}
		} );
	}

	public Map<Agent,String> shutdownAllContexts( List<Agent> endpoints ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.shutdownAllContexts();
			}
		} );
	}
	
	public Map<Agent,String> loadScript( 
		final List<Agent> endpoints, 
		final String contextName, 
		final String scriptName, 
		final String fileName, 
		final List<String> arguments ) 
			throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.loadScript( contextName, scriptName, fileName, arguments );
			}
		} );
	}
	
	public Map<Agent,String> startScript( 
		final List<Agent> endpoints, 
		final String language, 
		final String contextName, 
		final String scriptName, 
		final String fileName, 
		final List<String> arguments ) 
			throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.startScript( language, contextName, scriptName, fileName, arguments );
			}
		} );
	}
	
	public Map<Agent,String> status( List<Agent> endpoints ) 
		throws ServiceException
	{
		return runAgents( endpoints, new ParameterizedRunnable<BoxAgentHelper>() {
			public void run( BoxAgentHelper helper ) throws Exception {
				helper.status();
			}
		} );
	}

	private Map<Agent,String> runAgents( List<Agent> endpoints, ParameterizedRunnable<BoxAgentHelper> runnable ) throws ServiceException {
		try {
			Map<Agent,ByteArrayOutputStream> streams = createAgentStreams();
			BoxAgentHelper helper = createAgentHelper( endpoints, streams );
			runnable.run( helper );
			helper.join();
			return getAgentOutput( streams );
		}
		catch( Exception ex ) {
			throw new ServiceException( "Failed calling agents", ex );
		}
		
	}
	private Map<Agent,ByteArrayOutputStream> createAgentStreams() {
		return Collections.synchronizedMap( new HashMap<Agent,ByteArrayOutputStream>() );
	}

	private Map<Agent,String> getAgentOutput( Map<Agent,ByteArrayOutputStream> streams ) {
		Map<Agent,String> ret = new HashMap<Agent,String>();
		for( Map.Entry<Agent,ByteArrayOutputStream> entry : streams.entrySet() ) {
			ret.put( entry.getKey(), entry.getValue().toString() );
		}
		return ret;
	}
	private BoxAgentHelper createAgentHelper( final List<Agent> agents, final Map<Agent,ByteArrayOutputStream> streams ) {
		BoxAgentHelper agentHelper = new BoxAgentHelper();
		agentHelper.setStreamFactory( new PrintStreamFactory() {
			public PrintStream create( Endpoint endpoint ) {
				ByteArrayOutputStream bstream = new ByteArrayOutputStream();
				streams.put( getAgent(endpoint), bstream );
				return new PrintStream( bstream );
			}
		} );
		CompositeEndpointConnectionFactory<BoxInterface> connectionFactory = new CompositeEndpointConnectionFactory<BoxInterface>();
		connectionFactory.add( new BoxDirectConnectionFactory() );
		connectionFactory.add( new BoxTunnelConnectionFactory() );
		agentHelper.setEndpointConnectionFactory( connectionFactory );
	
		agentHelper.setEndpoints( getEndpoints(agents) );
		
		return agentHelper;
	}
	
	private List<Endpoint> getEndpoints( List<Agent> agents ) {
		List<Endpoint> endpoints = new ArrayList<Endpoint>( agents.size() );
		for( Agent agent : agents ) {
			endpoints.add( getEndpoint(agent) );
		}
		return endpoints;
	}
	
	private Endpoint getEndpoint( Agent agent ) {
		if( agentHeartbeatMapping == null ) {
			throw new RuntimeException( "Agents not loaded" );
		}
		Heartbeat<Endpoint> heartbeat = agentHeartbeatMapping.get( agent );
		if( heartbeat == null ) { 
			throw new RuntimeException( "No agent found: " + agent );
		}
		return heartbeat.getData();
	}
	
	private Agent getAgent( Endpoint endpoint ) {
		if( endpointAgentMapping == null ) {
			throw new RuntimeException( "Endpoints not loaded" );
		}
		Agent agent = endpointAgentMapping.get( endpoint );
		if( agent == null ) { 
			throw new RuntimeException( "No endpoint found: " + endpoint );
		}
		return agent;
	}
}

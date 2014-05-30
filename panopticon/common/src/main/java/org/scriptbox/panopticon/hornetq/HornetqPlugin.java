package org.scriptbox.panopticon.hornetq;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HornetqPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(HornetqPlugin.class);

	private Map<String,SimpleDateFormat> formats = new HashMap<String,SimpleDateFormat>();
	
	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(HornetqInjector.class));
		context.getBeans().put( "hornetq.factories", new HashMap<String,ClientSessionFactory>() );
		context.getBeans().put( "hornetq.sessions", new HashSet<ClientSession>() );
		super.contextCreated(context);
	}

	public void contextShutdown(BoxContext context) throws Exception {
		closeAllSessions();
		closeAllFactories();
	}

	private void closeAllFactories() {
		Map<String,ClientSessionFactory> factories = getFactories();
		for( ClientSessionFactory factory : factories.values() ) {
			try {
				factory.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing ClientSessionFactory", ex );
			}
		}
		factories.clear();
	}
	
	private void closeAllSessions() {
		Set<ClientSession> sessions = getSessions();
		for( ClientSession session : sessions ) {
			try {
				session.close();
			}
			catch( Exception ex ) {
				LOGGER.error( "Error closing ClientSession", ex );
			}
		}
		sessions.clear();
		
	}
	
	synchronized public void closeSession( ClientSession session ) throws Exception {
		try {
			if( !session.isClosed() ) {
				session.close();
			}
		}
		finally {
			getSessions().remove( session );
		}
	}
	
	synchronized public ClientSession getSession( String host, int port ) throws Exception {
		ClientSession session = getTransportSessionFactory(host, port).createSession();
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSession getSession( String host, int port, boolean autoCommitSends, boolean autoCommitAcks ) throws Exception {
		ClientSession session = getTransportSessionFactory(host, port).createSession(autoCommitSends,autoCommitAcks);
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSession getSession( String host, int port, boolean xa, boolean autoCommitSends, boolean autoCommitAcks ) throws Exception {
		ClientSession session = getTransportSessionFactory(host, port).createSession(xa,autoCommitSends,autoCommitAcks);
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSession getSession( String host, int port, 
		boolean xa, boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge ) throws Exception 
	{
		ClientSession session = getTransportSessionFactory(host, port).createSession(xa,autoCommitSends,autoCommitAcks,preAcknowledge);
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSession getSession( String host, int port, 
		boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge, int ackBatchSize ) throws Exception 
	{
		ClientSession session = getTransportSessionFactory(host, port).createSession(autoCommitSends,autoCommitAcks,ackBatchSize);
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSession getSession( String host, int port, 
		String user, String pass, boolean xa, boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge, int ackBatchSize ) throws Exception 
	{
		ClientSession session = getTransportSessionFactory(host, port).createSession(user,pass,xa,autoCommitSends,autoCommitAcks,preAcknowledge,ackBatchSize);
		getSessions().add( session );
		return session;
	}
	
	synchronized public ClientSessionFactory getTransportSessionFactory( String host, int port ) throws Exception {
		Map<String,ClientSessionFactory> factories = getFactories();
		String key = "transport:" + host + ":" + port;
		ClientSessionFactory factory = factories.get( key );
		if( factory == null ) {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put( "host", host );
			map.put( "port", port );
			TransportConfiguration configuration = 
				new TransportConfiguration(NettyConnectorFactory.class.getName(), map);
			ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(configuration);
			factory =  locator.createSessionFactory();
			factories.put( key, factory );
		}
		return factory;	
	}

	private Set<ClientSession> getSessions() {
		return BoxContext.getCurrentContext().getBeans().get( "hornetq.sessions", Set.class );
	}
	
	private Map<String,ClientSessionFactory> getFactories() {
		return BoxContext.getCurrentContext().getBeans().get( "hornetq.factories", Map.class );
	}
}

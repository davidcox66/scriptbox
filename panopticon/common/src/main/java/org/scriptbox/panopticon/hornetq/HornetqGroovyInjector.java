package org.scriptbox.panopticon.hornetq;

import org.codehaus.groovy.runtime.MethodClosure;
import org.hornetq.api.core.client.ClientSession;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HornetqGroovyInjector implements HornetqInjector {

	private static final Logger LOGGER = LoggerFactory.getLogger( HornetqGroovyInjector.class );
	
	private HornetqPlugin plugin;

	public HornetqPlugin getPlugin() {
		return plugin;
	}

	public void setPlugin(HornetqPlugin plugin) {
		this.plugin = plugin;
	}

	public String getLanguage() {
		return "groovy";
	}
	
	public void inject( BoxContext context ) {
		Lookup vars = context.getScriptVariables();
		vars.put( "hqSession", new MethodClosure( this, "hqSession")  );
		vars.put( "hqClose", new MethodClosure( this, "hqClose")  );
	}
	
	public ClientSession hqSession( String host, int port ) throws Exception {
		return plugin.getSession(host,port);
	}
	
	public ClientSession hqSession( String host, int port, boolean autoCommitSends, boolean autoCommitAcks ) throws Exception {
		return plugin.getSession(host,port,autoCommitSends,autoCommitAcks);
	}
	
	public ClientSession hqSession( String host, int port, boolean xa, boolean autoCommitSends, boolean autoCommitAcks ) throws Exception {
		return plugin.getSession(host,port,xa,autoCommitSends,autoCommitAcks);
	}
	
	public ClientSession hqSession( String host, int port, 
		boolean xa, boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge ) throws Exception 
	{
		return plugin.getSession(host,port,xa,autoCommitSends,autoCommitAcks,preAcknowledge);
	}
	
	public ClientSession hqSession( String host, int port, 
		boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge, int ackBatchSize ) throws Exception 
	{
		return plugin.getSession(host,port,autoCommitSends,autoCommitAcks,preAcknowledge,ackBatchSize);
	}
	
	public ClientSession hqSession( String host, int port, 
		String user, String pass, boolean xa, boolean autoCommitSends, boolean autoCommitAcks, boolean preAcknowledge, int ackBatchSize ) throws Exception 
	{
		return plugin.getSession(host,port,user,pass,xa,autoCommitSends,autoCommitAcks,preAcknowledge,ackBatchSize);
	}
	
	public void hqClose( ClientSession session ) throws Exception {
		plugin.closeSession(session);
	}
}

package org.scriptbox.ui.shared.agent;

import java.util.List;
import java.util.Map;

import org.scriptbox.util.cassandra.heartbeat.Heartbeat;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.scriptbox.util.remoting.endpoint.Endpoint;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/agentService")
public interface AgentGWTService extends RemoteService {

	public Map<String,List<Heartbeat<Endpoint>>> getAgentHeartbeats( List<String> groups ) throws ServiceException;
	
	public Map<Endpoint,String> createContext( List<Endpoint> endpoints, String language, String contextName ) throws ServiceException;
	public Map<Endpoint,String> startContext( List<Endpoint> endpoints, String contextName, List arguments ) throws ServiceException;
	public Map<Endpoint,String> stopContext( List<Endpoint> endpoints, String contextName ) throws ServiceException;
	public Map<Endpoint,String> shutdownContext( List<Endpoint> endpoints, String contextName ) throws ServiceException;
	public Map<Endpoint,String> shutdownAllContexts( List<Endpoint> endpoints ) throws ServiceException;
	public Map<Endpoint,String> loadScript( List<Endpoint> endpoints, String contextName, String scriptName, String fileName, List arguments ) throws ServiceException;
	public Map<Endpoint,String> startScript( List<Endpoint> endpoints, String language, String contextName, String scriptName, String fileName, List arguments ) throws ServiceException;
	public Map<Endpoint,String> status( List<Endpoint> endpoints ) throws ServiceException;
		
	
}

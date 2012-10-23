package org.scriptbox.ui.shared.agent;

import java.util.List;
import java.util.Map;

import org.scriptbox.util.gwt.server.remote.shared.ServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/agentService")
public interface AgentGWTService extends RemoteService {

	public List<Agent> getAgents( List<String> groups ) throws ServiceException;
	
	public Map<Agent,String> createContext( List<Agent> endpoints, String language, String contextName ) throws ServiceException;
	public Map<Agent,String> startContext( List<Agent> endpoints, String contextName, List arguments ) throws ServiceException;
	public Map<Agent,String> stopContext( List<Agent> endpoints, String contextName ) throws ServiceException;
	public Map<Agent,String> shutdownContext( List<Agent> endpoints, String contextName ) throws ServiceException;
	public Map<Agent,String> shutdownAllContexts( List<Agent> endpoints ) throws ServiceException;
	public Map<Agent,String> loadScript( List<Agent> endpoints, String contextName, String scriptName, String fileName, List arguments ) throws ServiceException;
	public Map<Agent,String> startScript( List<Agent> endpoints, String language, String contextName, String scriptName, String fileName, List arguments ) throws ServiceException;
	public Map<Agent,String> status( List<Agent> endpoints ) throws ServiceException;
		
	
}

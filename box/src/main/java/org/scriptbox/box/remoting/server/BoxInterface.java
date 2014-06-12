package org.scriptbox.box.remoting.server;

import java.util.List;
import java.util.Map;

public interface BoxInterface {

	public void createContext( String language, String contextName, Map<String,Object> properties ) throws Exception;
    public void startContext( String contextName, List arguments ) throws Exception; 
    public void stopContext( String contextName ) throws Exception; 
    public void shutdownContext( String contextName ) throws Exception; 
    public void shutdownAllContexts() throws Exception; 
    public void loadScript( String contextName, String scriptName, String script, List arguments ) throws Exception; 
    public String status() throws Exception;
}

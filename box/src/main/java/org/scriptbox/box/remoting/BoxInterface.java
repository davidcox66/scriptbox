package org.scriptbox.box.remoting;

import java.util.List;

public interface BoxInterface {

	public void createContext( String language, String contextName ) throws Exception;
    public void startContext( String contextName, List arguments ) throws Exception; 
    public void stopContext( String contextName ) throws Exception; 
    public void shutdownContext( String contextName ) throws Exception; 
    public void shutdownAllContexts() throws Exception; 
    public void loadScript( String contextName, String scriptName, String script, List arguments ) throws Exception; 
    public String status() throws Exception;
}

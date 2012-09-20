package org.scriptbox.box.remoting;

public interface BoxInterface {

	public void createContext( String language, String contextName ) throws Exception;
    public void startContext( String contextName ) throws Exception; 
    public void stopContext( String contextName ) throws Exception; 
    public void shutdownContext( String contextName ) throws Exception; 
    public void shutdownAllContexts() throws Exception; 
    public void loadScript( String contextName, String scriptName, String script, String[] args ) throws Exception; 
    public String status() throws Exception;
}

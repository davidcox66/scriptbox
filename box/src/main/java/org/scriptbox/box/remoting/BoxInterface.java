package org.scriptbox.box.remoting;

public interface BoxInterface {

	void createContext( String language, String contextName ) throws Exception;
    void startContext( String contextName ) throws Exception; 
    void stopContext( String contextName ) throws Exception; 
    void shutdownContext( String contextName ) throws Exception; 
    void shutdownAllContexts() throws Exception; 
    void loadScript( String contextName, String scriptName, String script, String[] args ) throws Exception; 
}

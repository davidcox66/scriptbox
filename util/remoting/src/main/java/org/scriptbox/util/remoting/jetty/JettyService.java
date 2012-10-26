package org.scriptbox.util.remoting.jetty;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.scriptbox.util.spring.context.ContextBuilder;
import org.eclipse.jetty.server.Server;

public class JettyService {

	private String hostname="127.0.0.1";
	private int port=8090;
	private boolean logging=false;
	private String logDirectory="/tmp";
	private String servletMapping="/remoting/*";
	private boolean jmx;
	
	private String jettyConfigLocation;
	private String contextConfigLocation;
	private ApplicationContext jettyContext;
	
	public JettyService() {
	}
	
	public JettyService( String contextConfigLocation ) {
		this( "classpath*:META-INF/spring/jetty-context.xml", contextConfigLocation );
	}
	
	public JettyService( String jettyConfigLocation, String contextConfigLocation ) {
		this.jettyConfigLocation = jettyConfigLocation;
		this.contextConfigLocation = contextConfigLocation;
	}
	
	public ApplicationContext start() {
		return start( new HashMap<String,Object>() );
	}
	public ApplicationContext start( String name, Object obj ) {
		Map<String,Object> vars = new HashMap<String,Object>();
		vars.put( name, obj );
		return start( vars );
	}
	public ApplicationContext start( Map<String,Object> vars ) {
		Map<String,Object> tmp = new HashMap<String,Object>( vars );
		tmp.put( "jetty", this );
		jettyContext = ContextBuilder.create(tmp, new String[] { jettyConfigLocation } );
		return jettyContext;
	}

	public Server getServer() {
		return jettyContext.getBean( "jettyServer", Server.class );
		
	}
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	public String getLogDirectory() {
		return logDirectory;
	}

	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}

	public boolean isJmx() {
		return jmx;
	}

	public void setJmx(boolean jmx) {
		this.jmx = jmx;
	}

	public String getJettyConfigLocation() {
		return jettyConfigLocation;
	}

	public void setJettyConfigLocation(String jettyConfigLocation) {
		this.jettyConfigLocation = jettyConfigLocation;
	}

	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	public ApplicationContext getJettyContext() {
		return jettyContext;
	}

	public void setJettyContext(ApplicationContext jettyContext) {
		this.jettyContext = jettyContext;
	}

	public String getServletMapping() {
		return servletMapping;
	}

	public void setServletMapping(String servletMapping) {
		this.servletMapping = servletMapping;
	}
	
	
}

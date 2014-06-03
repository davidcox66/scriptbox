package org.scriptbox.util.remoting.jetty;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

public class JettyService {

	private String hostname="127.0.0.1";
	private int port=8090;
	private boolean logging=false;
	private String logDirectory="/tmp";
	private String servletMapping="/remoting/*";
	private boolean jmx;
	
	private String contextConfigLocation;
	private String securityContextConfigLocation;
	
	public JettyService() {
	}
	
	public JettyService( String contextConfigLocation ) {
		this( contextConfigLocation, null );
	}
	
	public JettyService( String contextConfigLocation, String securityContextConfigLocation ) {
		this.contextConfigLocation = contextConfigLocation;
		this.securityContextConfigLocation = securityContextConfigLocation;
	}

	public void start() throws Exception {
		getServer().start();
	}
	
	public Server getServer() {
		Server server = new Server();
		
		SelectChannelConnector conn = new SelectChannelConnector();
		server.addConnector( conn );
		conn.setHost( hostname );
		conn.setPort( port );
	
		HandlerCollection handlers = new HandlerCollection();
		server.setHandler(handlers);;
		
		ServletContextHandler servletContext = new ServletContextHandler();
		servletContext.setContextPath("/");
		servletContext.setSessionHandler( new SessionHandler() );
		servletContext.setResourceBase( "/var/www" );
		handlers.addHandler( servletContext );
		
		ServletHandler servlets = new ServletHandler();
		servletContext.setServletHandler( servlets );
		
		ServletHolder remoting = new ServletHolder();
		remoting.setName( "RemotingServlet" );
		remoting.setServlet( new DispatcherServlet() );
		remoting.setInitParameter("resourceBase", "/var/www" );
		remoting.setInitParameter("contextConfigLocation", contextConfigLocation);
		servlets.addServlet( remoting );
		
		ServletMapping mapping = new ServletMapping();
		mapping.setPathSpec( servletMapping );
		mapping.setServletName( "RemotingServlet" );
		servlets.addServletMapping( mapping );

		if( securityContextConfigLocation != null ) {
			FilterHolder filter = new FilterHolder();
			filter.setClassName("org.springframework.web.filter.DelegatingFilterProxy");
			filter.setName("springSecurityFilterChain");
			servletContext.addFilter(filter, "/*", EnumSet.of(DispatcherType.INCLUDE,DispatcherType.REQUEST));
			servletContext.addEventListener( new ContextLoaderListener() );
			servletContext.getInitParams().put("contextConfigLocation", securityContextConfigLocation );
		}
		
		if( logging ) {
			RequestLogHandler log = new RequestLogHandler();
			handlers.addHandler(log);
			NCSARequestLog nlog = new NCSARequestLog();
			log.setRequestLog(nlog);
			nlog.setAppend( true );
			nlog.setExtended( true );
			nlog.setRetainDays( 999 );
			nlog.setFilenameDateFormat( "yyyy-MM-dd" );
			nlog.setFilename( logDirectory + "/request.log.yyyy_mm_dd" );
		}
		
		if( jmx ) {
			JettyJmxInitializer initializer = new JettyJmxInitializer();
			initializer.setServer( server );
		}
		
		
		return server;
		
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

	public String getContextConfigLocation() {
		return contextConfigLocation;
	}

	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}

	public String getSecurityContextConfigLocation() {
		return securityContextConfigLocation;
	}

	public void setSecurityContextConfigLocation(
			String securityContextConfigLocation) {
		this.securityContextConfigLocation = securityContextConfigLocation;
	}

	public String getServletMapping() {
		return servletMapping;
	}

	public void setServletMapping(String servletMapping) {
		this.servletMapping = servletMapping;
	}
	
	
}

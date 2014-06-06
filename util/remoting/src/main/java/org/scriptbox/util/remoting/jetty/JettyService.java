package org.scriptbox.util.remoting.jetty;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

public class JettyService {

	private String hostname="127.0.0.1";
	private int httpPort=8090;
	private int httpsPort=0;
	
	private boolean logging=false;
	private String logDirectory="/tmp";
	private String servletMapping="/remoting/*";
	private boolean jmx;
	
	private String keyStorePath;
	private String keyStorePassword;
	private String certificatePassword;
	private String certificateAlias;
	private String trustStorePath;
	private String trustStorePassword;
	
	private String contextConfigLocation;
	private String securityContextConfigLocation;
	
	private Map<String,File> directories = new HashMap<String,File>();
	
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

		HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setSecurePort(httpsPort);
        
		if( httpPort > 0 ) {
	        ServerConnector conn = new ServerConnector(server,new HttpConnectionFactory(httpConfig));        
	        conn.setPort(httpPort);
			server.addConnector( conn );
		}
	
		if( httpsPort > 0 ) {
			HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
	        httpsConfig.addCustomizer(new SecureRequestCustomizer());
	 
			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath( getKeyStoreLocation() );
			sslContextFactory.setKeyStorePassword( keyStorePassword != null ? keyStorePassword : "password" );
			sslContextFactory.setKeyManagerPassword( certificatePassword != null ? certificatePassword : "password" );
			sslContextFactory.setCertAlias( certificateAlias != null ? certificateAlias : "jetty" );
			
			if( trustStorePath != null ) {
				sslContextFactory.setTrustStorePath(trustStorePath);
			}
			if( trustStorePassword != null ) {
				sslContextFactory.setTrustStorePassword(trustStorePassword);
			}
			
	        ServerConnector conn = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,"http/1.1"), new HttpConnectionFactory(httpsConfig));
	        conn.setPort(httpsPort);
			server.addConnector(conn);
			
		}
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
		// remoting.setInitParameter("resourceBase", "/var/www" );
		remoting.setInitParameter("contextConfigLocation", contextConfigLocation);
		servlets.addServlet( remoting );
		
		ServletMapping mapping = new ServletMapping();
		mapping.setPathSpec( servletMapping );
		mapping.setServletName( "RemotingServlet" );
		servlets.addServletMapping( mapping );

		if( directories.size() > 0 ) {
			int i=0;
			for( Map.Entry<String,File> entry : directories.entrySet() ) {
				String pathSpec = entry.getKey();
				File directory = entry.getValue();
				String name = "Directory_" + i++;
				
				ServletHolder holder = new ServletHolder();
				holder.setName( name );
				holder.setServlet( new DefaultServlet() );
				holder.setInitParameter("resourceBase", directory.getAbsolutePath() );
				holder.setInitParameter("dirAllowed", "true" );
				servlets.addServlet( holder );
				
				mapping = new ServletMapping();
				mapping.setPathSpec( pathSpec );
				mapping.setServletName( name );
				servlets.addServletMapping( mapping );
			}
		}
		
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

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(int httpsPort) {
		this.httpsPort = httpsPort;
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
	
	
	public String getKeyStorePath() {
		return keyStorePath;
	}

	public void setKeyStorePath(String keyStorePath) {
		this.keyStorePath = keyStorePath;
	}

	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	public String getCertificatePassword() {
		return certificatePassword;
	}

	public void setCertificatePassword(String certificatePassword) {
		this.certificatePassword = certificatePassword;
	}

	public String getCertificateAlias() {
		return certificateAlias;
	}

	public void setCertificateAlias(String certificateAlias) {
		this.certificateAlias = certificateAlias;
	}

	public String getTrustStorePath() {
		return trustStorePath;
	}

	public void setTrustStorePath(String trustStorePath) {
		this.trustStorePath = trustStorePath;
	}

	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}
	
	public void addDirectory( String pathSpec, File directory ) {
		directories.put( pathSpec, directory );
	}

	public String getKeyStoreLocation() {
		if( StringUtils.isNotBlank(keyStorePath) ) {
			return keyStorePath;
		}
		else {
			File file = new File("keystore");
			if( file.exists() ) {
				return file.getAbsolutePath();
			}
			file = new File(System.getProperty("user.home") + File.separator + ".scriptbox" + File.separator + "keystore" );
			if( file.exists() ) {
				return file.getAbsolutePath();
			}
			throw new RuntimeException( "No keystore path defined and none found in current directory or $HOME/.scriptbox");
		}
	}
	
}

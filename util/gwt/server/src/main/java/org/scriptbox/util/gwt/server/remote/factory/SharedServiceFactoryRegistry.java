package org.scriptbox.util.gwt.server.remote.factory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.scriptbox.util.gwt.server.remote.locator.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * The registry functionality has been moved out of GWTRemoteServiceToSpringBridgeServlet as some servlet containers
 * seem to be instantiating supposed 'singleton' servlets for each request, causing the costly registry functionality to
 * be re-initialized. This class moves the registry to a true singleton. 
 * 
 */
public class SharedServiceFactoryRegistry implements ServiceFactoryRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger( SharedServiceFactoryRegistry.class );
    
    private WebApplicationContext applicationContext;
    private static SharedServiceFactoryRegistry instance;
    private Map<String, ServiceFactory> factories = new HashMap<String, ServiceFactory>();
    
    public static SharedServiceFactoryRegistry getInstance( ServletContext servletContext ) {
        // Not worried about synchronization to prevent an extra instantiation
        if( instance == null ) {
            instance = new SharedServiceFactoryRegistry( servletContext );
        }
        return instance;
    }
    private SharedServiceFactoryRegistry( ServletContext servletContext ) {
	    applicationContext = WebApplicationContextUtils.getWebApplicationContext( servletContext );
        final Map<String, ServiceLocator> locators = applicationContext.getBeansOfType( ServiceLocator.class );
        LOGGER.debug( "init: initializing registry - locators=" + locators );
        if ( locators != null && !locators.isEmpty() ) {
            for( ServiceLocator locator : locators.values() ) {
                locator.locate( this );
            }
        }
        else {
            throw new RuntimeException( "No GWTServiceLocators defined in application context");
        }
    }
    
    @Override
    public WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void addService( String name, ServiceFactory  factory ) {
        LOGGER.debug( "{} is being mapped to a {}", name, factory );
        factories.put( name, factory );
    }
 
    public Object getBean( HttpServletRequest request ) throws Exception {
        String url = request.getRequestURI();
        int lastSlash = url.lastIndexOf( "/" );
        String service = url.substring( lastSlash + 1 );
        ServiceFactory factory = factories.get( service );
        if( factory == null ) {
        	throw new RuntimeException( "Could not find factory for service - url=" + url + ", service=" + service );
        }
        Object handler = factory.getInstance( request );
        if ( handler == null ) {
            throw new RuntimeException( "No such service available: " + service );
        }
        return handler;
    }
}

package org.scriptbox.util.gwt.server.remote.factory;


import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.WebApplicationContext;

/**
 * This represents an object which will manage installing services for GWT-RPC. This may be implemented by the remoting
 * servlet itself. Though it may be separated as some servlet containers instantiate the servlet multiple times, resulting
 * in multiple unwanted instantiations of the registry.
 *
 * The registry also handles looking up these services based upon the given request url and comparing it against the names
 * of previously registered services. This service bean is passed to GWT-RPC which will then delegate the request to the
 * service.
 * 
 */
public interface ServiceFactoryRegistry {

    public WebApplicationContext getApplicationContext();
    public void addService( String name, ServiceFactory factory );
    public Object getBean( HttpServletRequest request ) throws Exception;  
}

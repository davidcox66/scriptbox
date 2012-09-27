package org.scriptbox.util.gwt.server.remote.locator;

import org.scriptbox.util.gwt.server.remote.factory.ServiceFactoryRegistry;

/**
 * This interface decouples the work of finding eligible services exposed through GWT-RPC from the plumbing
 * required to install the service and make it visible to GWT-RPC dispatching. 
 * This allows the GWTRemoteServiceToSpringBridgeServlet to support multiple
 * sources for services and keeping its implementation simpler.
 * 
 */
public interface ServiceLocator {

    public void locate( ServiceFactoryRegistry registry );
}

package org.scriptbox.util.gwt.server.remote.locator;

import org.scriptbox.util.gwt.server.remote.ServiceScope;

/**
 * A bean to supply all the information needed to register any spring bean as a GWT-RPC service. This is suitable
 * in cases where the annotation route is not feasible. In the case of webservices, the WS client proxy class is being
 * generated so we have no choice except to manually construct the service specification. The ServiceFactoryRegistry
 * will take care of installing the service with the supplied interfaces in a java proxy wrapper.
 * 
 */
public class ServiceSpecification {

    private String name;
    private ServiceScope scope;
    private Class<?>[] interfaceTypes;
    private Object serviceInstance;
   
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ServiceScope getScope() {
		return scope;
	}
	public void setScope(ServiceScope scope) {
		this.scope = scope;
	}
	public Object getServiceInstance() {
        return serviceInstance;
    }
    public void setServiceInstance(Object serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
    public Class<?>[] getInterfaceTypes() {
        return interfaceTypes;
    }
    public void setInterfaceTypes(Class<?>[] interfaceTypes) {
        this.interfaceTypes = interfaceTypes;
    }
    
}

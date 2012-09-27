package org.scriptbox.util.gwt.server.remote.factory;

import javax.servlet.http.HttpServletRequest;

import org.scriptbox.util.gwt.server.remote.ServiceAop;
import org.scriptbox.util.gwt.server.remote.ServiceScope;


public abstract class ScopedServiceFactory implements ServiceFactory {

	protected String name;
	protected ServiceScope scope;
	protected Class<?>[] types;
	
	public ScopedServiceFactory( String name, ServiceScope scope, Class<?>[] types ) {
		this.name = name;
		this.scope = scope;
		this.types = types;
	}
	
	public Object getInstance(HttpServletRequest request) throws Exception {
		Object instance = null;
		if( scope != ServiceScope.NONE ) {
			Object lock = scope.getLock(request);
			synchronized( lock ) {
				instance = scope.get( request, name );
				if( instance == null ) {
					instance = ServiceAop.wrapHandlerIfNeeded( createInstance(), types );
					scope.set(request, name, instance );
				}
			}
		}
		else {
			instance = ServiceAop.wrapHandlerIfNeeded( createInstance(), types );
		}
		return instance;
	}
	
	protected abstract Object createInstance() throws Exception;
}

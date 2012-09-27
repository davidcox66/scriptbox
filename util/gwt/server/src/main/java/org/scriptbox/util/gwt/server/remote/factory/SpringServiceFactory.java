package org.scriptbox.util.gwt.server.remote.factory;

import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class SpringServiceFactory extends ScopedServiceFactory implements ApplicationContextAware {

	private ApplicationContext context;
	
	public SpringServiceFactory( String name, ServiceScope scope, Class<?>[] types ) {
		super( name, scope, types );
	}
	
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
	
	@Override
	protected Object createInstance() throws Exception {
		return context.getBean( name );
	}

}

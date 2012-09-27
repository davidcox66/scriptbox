package org.scriptbox.util.gwt.server.remote.locator;

import java.util.List;

import org.scriptbox.util.gwt.server.remote.factory.ScopedServiceFactory;
import org.scriptbox.util.gwt.server.remote.factory.ServiceFactoryRegistry;
import org.scriptbox.util.gwt.server.remote.factory.SpringServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * A GWT-RPC service locater that is given an explicit list of services to expose in the application context.
 * 
 */
public class SpecificationServiceLocator implements ServiceLocator, ApplicationContextAware {

    @SuppressWarnings("unused")
	private static final Logger sLogger = LoggerFactory.getLogger( SpecificationServiceLocator.class );
    
    private List<ServiceSpecification> specifications;
    private ApplicationContext context;
    
    @Override
    public void locate(ServiceFactoryRegistry registry) {
        for ( ServiceSpecification specification : specifications ) { 
        	SpringServiceFactory factory = new SpringServiceFactory( specification.getName(), specification.getScope(), specification.getInterfaceTypes() );
        	factory.setApplicationContext(context);
            registry.addService( specification.getName(), factory );
        }
    }

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
	
    public List<ServiceSpecification> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(List<ServiceSpecification> specifications) {
        this.specifications = specifications;
    }
    
    

}

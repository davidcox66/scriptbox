package org.scriptbox.util.gwt.server.remote.locator;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.factory.ServiceFactoryRegistry;
import org.scriptbox.util.gwt.server.remote.factory.SpringServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * This searches for spring beans that are annotated with the desired annotation (default of @RemoteService).
 * Any matching beans should have a @RemoteService annotation that specifies the name and optional types
 * for the service interface.
 * 
 */
public class AnnotationServiceLocator implements ServiceLocator, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger( AnnotationServiceLocator.class );
    
    private Class<? extends Annotation> annotationClass;
    
	private ApplicationContext context;
	
    public AnnotationServiceLocator() {
        this( ExportableService.class );
    }
   
    public AnnotationServiceLocator( Class<? extends Annotation> annotationClass ) {
       this.annotationClass = annotationClass; 
    }
    
    public Class<? extends Annotation> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
	
    @Override
    public void locate(ServiceFactoryRegistry registry) {
        final Map<String, Object> services = registry.getApplicationContext().getBeansWithAnnotation( annotationClass );
        if ( services == null || services.isEmpty() ) {
            LOGGER.warn( "locate: no service beans found" );
        }
        else {
	    	if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "locate: found " + services.size() + " services"); }
    	}
        for ( Map.Entry<String, Object> entry : services.entrySet() ) {
            Object handler = entry.getValue();
            ExportableService ann = handler.getClass().getAnnotation( ExportableService.class );
            String name = ann.value();
            if ( name == null || name.isEmpty() ) {
                name = entry.getKey();
            }
            if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "locate: registering service factory - name=" + name + ", scope=" + ann.scope() + ", types=" + ann.types() ); }
            SpringServiceFactory factory = new SpringServiceFactory(name, ann.scope(), ann.types() );
            factory.setApplicationContext(context);
            registry.addService( name, factory );
        }
    }

}

package org.scriptbox.util.gwt.server.remote;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a spring bean to be the POJO implementation of a Remote Service.  This is designed to be used with any
 * browser to server remoting mechanism.  The first implementation will be done with GWT.
 * 
 */
@Inherited
@Documented
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface ExportableService {
    /**
     * @return a unique name to identify the annotated service. This will be used to identify it when mapping
     * a service call to its implementation.
     */
    String value() default "";
    
    /**
     * @return Remoting may require wrapping the service object in a proxy. This collection of types
     * specifies which interfaces would need to be exposed on the proxy should the service need
     * to be proxied.
     */
    Class<?>[] types() default {}; 
    
    ServiceScope scope() default ServiceScope.NONE;
}

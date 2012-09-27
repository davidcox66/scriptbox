package org.scriptbox.util.gwt.server.remote;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceAop {

    private static final Logger LOGGER = LoggerFactory.getLogger( ServiceAop.class );
    
    /**
     * Will wrap the given handler in a java proxy if it is required. Currently this would only be required
     * if handler did not implement the RemoteService interface. If wrapped, it will try to use the supplied
     * interfaceTypes for the proxy. The RemoteService interface will be added to the proxy class if not
     * specified in the list of interfaceTypes.
     * 
     * @param handler
     * @param interfaceTypes
     * @return
     */
    public static Object wrapHandlerIfNeeded( Object handler, Class<?>[] interfaceTypes ) {
        if( isProxyWrapperNeeded(handler) ) {
            if( interfaceTypes == null ) {
                interfaceTypes = handler.getClass().getInterfaces();
                LOGGER.debug( "wrappHandlerIfNeeded: no interface types specified, trying handler class interfaces: {}", interfaceTypes );
            }
            if( interfaceTypes == null || interfaceTypes.length == 0 ) {
               throw new RuntimeException( "Cannot wrap GWT service class without at least one interface specified " +
                   "or an interface defined directly on the class: " + handler.getClass() );
            }
            List<Class<?>> types = Arrays.asList( interfaceTypes ); 
            if( !isAnyRemoteServiceClass(interfaceTypes) ) {
                types.add( com.google.gwt.user.client.rpc.RemoteService.class );
            }
            LOGGER.info( "wrapHandlerIfNeeded: wrapping handler=" + handler + ", types=" + types );
            final Object delegate = handler;
            handler = Proxy.newProxyInstance( ServiceAop.class.getClassLoader(), (Class[])types.toArray(),
              new InvocationHandler() {
                @Override
                public Object invoke( Object o, Method method, Object[] os ) throws Throwable {
                    LOGGER.debug( "invoke: calling service method {}", method );
                    return method.invoke( delegate, os );
                }
            } );
        }
        return handler; 
    }
 
    /** 
     * We've defined this to simply check if the services is an instance of RemoteInterface. We kept this method
     * separate in case we would like to wrap for other more sophisticated reasons reasons.
     * 
     * @param obj
     * @return
     */
    public static boolean isProxyWrapperNeeded( Object obj ) {
        return !isRemoteServiceClass( obj.getClass() );
    }

    /**
     * This is used when proxying a class to determine if any of the supplied classes implement the GWT
     * RemoteService interface. The consequence of not have any would be to add RemoteService as an
     * additional interface of the proxy.
     * 
     * @param clss
     * @return
     */
    public static boolean isAnyRemoteServiceClass( Class<?>[] clss ) {
        if( clss != null ) {
            for( Class<?> cls : clss ) {
                if( isRemoteServiceClass(cls) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the given class has implemented the GWT RemoteService interface somewhere in its
     * class hierarchy.
     * 
     * @param cls
     * @return
     */
    public static boolean isRemoteServiceClass( Class<?> cls ) {
        return com.google.gwt.user.client.rpc.RemoteService.class.isAssignableFrom(cls);
    }
    


}

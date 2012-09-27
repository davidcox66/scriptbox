package org.scriptbox.util.gwt.server.remote;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.scriptbox.util.gwt.server.remote.factory.ServiceFactoryRegistry;
import org.scriptbox.util.gwt.server.remote.factory.SharedServiceFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This servlet dispatches GWT-RPC service calls to previously registered services in a spring application context.  
 * The registration mechanism of defining named services is delegated to a shared instance of a ServiceFactoryRegistry.
 * The registry is responsible for the mapping of GWT-RPC request urls to service objects. This uses a singleton
 * implementation as some containers, such as the one used by GWT during development, instantiate a new servlet
 * per request.
 *
 * The registry is responsible for installing necessary services. The registry in turn relies upon ServiceLocator 
 * objects to locate eligible services. SharedServiceFactoryRegistry will locate these by finding all ServiceLocator
 * objects in the application context.
 * 
 */
public class ServiceInstanceServlet extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger( ServiceInstanceServlet.class );

    private ServiceFactoryRegistry registry;
    
    public ServiceInstanceServlet() {
        LOGGER.info( "Created ServiceInstanceServlet" );
    }
    
    @Override
    public void init( ServletConfig config ) throws ServletException {
    	LOGGER.info( "init: initializing service instance servlet");
        super.init( config );
        registry = SharedServiceFactoryRegistry.getInstance( getServletContext() );
    }

    @Override
    public String processCall( String payload ) throws SerializationException {
        try {
            LOGGER.debug( "processing RPC request" );
            Object handler = registry.getBean( getThreadLocalRequest() );
            RPCRequest rpcRequest = RPC.decodeRequest( payload, handler.getClass(), this );
            onAfterRequestDeserialized( rpcRequest );
            return RPC.invokeAndEncodeResponse( handler, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest.
                getSerializationPolicy() );
        } 
        catch ( IncompatibleRemoteServiceException ex ) {
            LOGGER.error( "An IncompatibleRemoteServiceException was thrown while processing this call.", ex );
            return RPC.encodeResponseForFailure( null, ex );
        } 
        catch ( RuntimeException ex ) {
            LOGGER.error( "A RuntimeException was thrown while processing this call.", ex );
            throw ex;
        }
        catch( Exception ex ) {
            LOGGER.error( "An Exception was thrown while processing this call.", ex );
        	throw new RuntimeException( "An Exception was thrown while processing this call: " + ex.getMessage() );
        }
    }
}

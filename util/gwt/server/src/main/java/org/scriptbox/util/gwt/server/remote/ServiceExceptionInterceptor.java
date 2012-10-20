package org.scriptbox.util.gwt.server.remote;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.scriptbox.util.common.error.ExceptionHelper;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceExceptionInterceptor implements MethodInterceptor {

	private static final Logger LOGGER = LoggerFactory.getLogger( ServiceExceptionInterceptor.class );
	
	private boolean stackTracesEnabled;
	
	public boolean isStackTracesEnabled() {
		return stackTracesEnabled;
	}


	public void setStackTracesEnabled(boolean stackTracesEnabled) {
		this.stackTracesEnabled = stackTracesEnabled;
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		}
		catch( ServiceException ex ) {
			String msg = "Unhandled exception in service: " + invocation.getMethod();
			LOGGER.error( msg, ex );
			if( ex.getCause() != null ) {
				ServiceException sex = new ServiceException( ex.getMessage() );
				if( isStackTracesEnabled() )  {
					ex.setExceptionTraceString( ExceptionHelper.toString(ex) );
				}
				throw sex;
			}
			else {
				if( isStackTracesEnabled() )  {
					ex.setExceptionTraceString( ExceptionHelper.toString(ex) );
				}
				throw ex;
			}
		}
		catch( Exception ex ) {
			String msg = "Unhandled exception in service: " + invocation.getMethod();
			LOGGER.error( msg, ex );
			ServiceException sex =  new ServiceException( ex.getMessage() );
			if( isStackTracesEnabled() ) {
				sex.setExceptionTraceString( ExceptionHelper.toString(ex) );
			}
			throw sex;
		}
	}

}

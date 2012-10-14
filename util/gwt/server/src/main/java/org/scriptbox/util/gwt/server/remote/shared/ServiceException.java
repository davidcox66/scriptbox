package org.scriptbox.util.gwt.server.remote.shared;

import java.io.Serializable;


public class ServiceException extends Exception implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String exceptionTraceString;

	public ServiceException() {
	}
	
	public ServiceException( String message ) {
		super( message );
	}
	
	public ServiceException( String message, Throwable cause ) {
		super( message, cause );
	}
	
	public String getExceptionTraceString() {
		return exceptionTraceString;
	}

	public void setExceptionTraceString(String exceptionTraceString) {
		this.exceptionTraceString = exceptionTraceString;
	}

	
}

package org.scriptbox.horde.action;

public class ActionScriptAbortException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ActionScriptAbortException() {
	}
	
	public ActionScriptAbortException( String message ) {
		super( message );
	}
	
	public ActionScriptAbortException( String message, Throwable cause ) {
		super( message, cause );
	}
}

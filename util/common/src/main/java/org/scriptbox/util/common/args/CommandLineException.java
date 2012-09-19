package org.scriptbox.util.common.args;

public class CommandLineException extends Exception {

	private static final long serialVersionUID = 1L;

	public CommandLineException( String message ) {
		super( message );
	}
	
	public CommandLineException( String message, Throwable cause ) {
		super( message, cause );
	}
}

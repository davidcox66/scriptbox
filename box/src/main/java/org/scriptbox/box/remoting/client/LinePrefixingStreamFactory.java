package org.scriptbox.box.remoting.client;

import java.io.PrintStream;

import org.scriptbox.util.remoting.endpoint.Endpoint;

public class LinePrefixingStreamFactory implements PrintStreamFactory {

	private PrintStream delegate;
	private String prefix;
	
	public LinePrefixingStreamFactory( PrintStream delegate, String prefix ) {
		this.delegate = delegate;
		this.prefix = prefix;
	}
	
	public PrintStream getOutputStream(Endpoint endpoint) {
		return new LinePrefixingPrintStream( delegate, prefix );
	}

}

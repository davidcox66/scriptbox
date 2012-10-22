package org.scriptbox.box.remoting.client;

import java.io.PrintStream;

import org.scriptbox.util.remoting.endpoint.Endpoint;

public interface PrintStreamFactory {

	public PrintStream create( Endpoint endpoint );
}

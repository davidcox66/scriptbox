package org.scriptbox.box.remoting.client;

import java.io.PrintStream;

import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.lang.StringUtils;

public class EndpointPrefixingStreamFactory implements PrintStreamFactory, InitializingBean {

	private PrintStream delegate;

	public EndpointPrefixingStreamFactory() {
		this.delegate = System.out;
	}
	
	public EndpointPrefixingStreamFactory( PrintStream delegate ) {
		this.delegate = delegate;
	}
	
	public PrintStream create(Endpoint endpoint) {
		return new LinePrefixingPrintStream( delegate, endpoint.getIdentifier() + " : " );
	}

	public PrintStream getDelegate() {
		return delegate;
	}

	public void setDelegate(PrintStream delegate) {
		this.delegate = delegate;
	}

	public void afterPropertiesSet() throws Exception {
		if( delegate == null ) {
			throw new IllegalArgumentException( "Delegate must be specified");
		}
	}
}

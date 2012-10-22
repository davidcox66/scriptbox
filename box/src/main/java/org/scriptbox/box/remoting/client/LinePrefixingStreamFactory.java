package org.scriptbox.box.remoting.client;

import java.io.PrintStream;

import org.scriptbox.util.remoting.endpoint.Endpoint;
import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.lang.StringUtils;

public class LinePrefixingStreamFactory implements PrintStreamFactory, InitializingBean {

	private PrintStream delegate;
	private String prefix;

	public LinePrefixingStreamFactory() {
		this.delegate = System.out;
	}
	
	public LinePrefixingStreamFactory( PrintStream delegate, String prefix ) {
		this.delegate = delegate;
		this.prefix = prefix;
	}
	
	public PrintStream create(Endpoint endpoint) {
		return new LinePrefixingPrintStream( delegate, prefix );
	}

	public PrintStream getDelegate() {
		return delegate;
	}

	public void setDelegate(PrintStream delegate) {
		this.delegate = delegate;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void afterPropertiesSet() throws Exception {
		if( delegate != null ) {
			throw new IllegalArgumentException( "Delegate must be specified");
		}
		if( StringUtils.isEmpty(prefix) ) {
			throw new IllegalArgumentException( "Prefix must be specified");
		}
	}
}

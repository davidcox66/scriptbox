package org.scriptbox.util.remoting.endpoint;

public interface Connection<X> {

	public X getProxy();
	public Endpoint getEndpoint();
	public String getIdentifier();
	public void close() throws Exception;
}

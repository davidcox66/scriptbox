package org.scriptbox.horde.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class Http {

	protected HttpClient client;
	protected CookieStore cookieStore;
	protected HttpContext localContext;

	public Http() {
		client = new DefaultHttpClient();
		cookieStore = new BasicCookieStore();
		localContext = new BasicHttpContext();
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public void getAndConsume( String url ) throws ClientProtocolException, IOException {
		HttpResponse response = get( url );
		consume( response );
	}
	
	public void postAndConsume( String url ) throws ClientProtocolException, IOException {
		HttpResponse response = post( url );
		consume( response );
	}
	
	public HttpResponse get( String url ) throws ClientProtocolException, IOException {
		HttpGet op = new HttpGet( url ); 
		return client.execute( op, localContext );
	}
	
	public HttpResponse post( String url ) throws ClientProtocolException, IOException {
		HttpPost op = new HttpPost( url ); 
		return client.execute( op, localContext );
	}
	
	public HttpResponse execute( HttpUriRequest request ) throws ClientProtocolException, IOException {
		return client.execute( request, localContext );
	}

	public void consume( HttpResponse response ) throws IOException {
		HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);	
	}
	
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}
	
	public HttpClient getClient() {
		return client;
	}

	public HttpContext getContext() {
		return localContext;
	}
}

package org.scriptbox.selenium;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpConnect implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnect.class);

    private String localDownloadPath = System.getProperty("java.io.tmpdir");
    private SeleniumService service;
    private boolean followRedirects = true;
    private boolean useCookies = true;
    private int httpStatus = 0;

    public HttpConnect(SeleniumService service) {
        this.service = service;
    }

    public HttpConnect setFollowRedirects(boolean value) {
        this.followRedirects = value;
        return this;
    }

    public String getLocalDownloadPath() {
        return this.localDownloadPath;
    }

    public HttpConnect setLocalDownloadPath(String filePath) {
        this.localDownloadPath = filePath;
        return this;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public HttpConnect setUseCookies(boolean value) {
        this.useCookies = value;
        return this;
    }

    /**
     * Load in all the cookies WebDriver currently knows about so that we can mimic the browser cookie state
     *
     * @param seleniumCookieSet
     * @return
     */
    private BasicCookieStore mimicCookieState(Set<Cookie> seleniumCookieSet) {
        BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
        for ( Cookie seleniumCookie : seleniumCookieSet) {
            BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            duplicateCookie.setDomain(seleniumCookie.getDomain());
            duplicateCookie.setSecure(seleniumCookie.isSecure());
            duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
            duplicateCookie.setPath(seleniumCookie.getPath());
            mimicWebDriverCookieStore.addCookie(duplicateCookie);
        }
        return mimicWebDriverCookieStore;
    }

    public HttpResponse get( String url ) throws URISyntaxException, IOException {
        return get( url, null, null );
    }

    public HttpResponse get( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        return execute( buildGet(url,params,headers) );
    }

    public HttpGet buildGet( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        RequestConfig.Builder builder = getBuilder();
        HttpGet ret = new HttpGet( buildUri(url,params) );
        ret.setConfig( builder.build() );
        addHeaders( ret, headers );
        return ret;
    }

    public URI buildUri( String url, Map<String,String> params ) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder( url );
        if( params != null ) {
            for( Map.Entry<String,String> p : params.entrySet() ) {
                ub.setParameter( p.getKey(), p.getValue() );
            }
        }
        return ub.build();
    }

    public void addHeaders( AbstractHttpMessage message, Map<String,String> headers ) {
        if( headers != null ) {
            for( Map.Entry<String,String> h : headers.entrySet() ) {
                message.addHeader( new BasicHeader(h.getKey(),h.getValue()));
            }
        }
    }
    public HttpResponse execute( HttpRequestBase request ) throws IOException {
        HttpClient client = getClient();
        BasicHttpContext localContext = getContext();
        return client.execute( request, localContext );
    }

    /**
     * Perform the file/image download.
     *
     * @param element
     * @param attribute
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    public File download(WebElement element, String attribute) throws IOException, NullPointerException, URISyntaxException {
        URL url = getDownloadUrl(element, attribute );
        File file = getDownloadFile( url );

        LOGGER.info("Sending GET request for: " + url );
        HttpResponse response = get( url.toString() );
        this.httpStatus = response.getStatusLine().getStatusCode();
        LOGGER.info("HTTP GET request status: " + this.httpStatus);

        LOGGER.info("Downloading file: " + file.getName());
        write( response, file );
        return file;
    }

    public void write( HttpResponse response, File file ) throws IOException {
        write( response.getEntity().getContent(), file );
    }

    public void write( InputStream stream, File file ) throws IOException {
        FileUtils.copyInputStreamToFile(stream, file);
        stream.close();
    }

    public String slurp( HttpResponse response ) throws IOException {
        return slurp( response.getEntity().getContent() );
    }
    public String slurp( InputStream stream ) throws IOException {
        String ret = IOUtils.toString( stream );
        stream.close();
        return ret;
    }

    public HttpClient getClient() {
        return HttpClientBuilder.create().build();
    }

    public BasicHttpContext getContext() {
        BasicHttpContext localContext = new BasicHttpContext();
        if (this.useCookies) {
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, mimicCookieState(service.getCookies()));
        }
        return localContext;
    }

    public RequestConfig.Builder getBuilder() {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setRedirectsEnabled(true);
        return builder;
    }

    public File getDownloadFile( URL downloadUrl ) {
        File downloadedFile = new File(this.localDownloadPath + downloadUrl.getFile().replaceFirst("/|\\\\", ""));
        if (downloadedFile.canWrite() == false) {
            downloadedFile.setWritable(true);
        }
        return downloadedFile;
    }

    public URL getDownloadUrl( WebElement element, String attribute ) throws MalformedURLException {
        String fileToDownloadLocation = element.getAttribute(attribute);
        if (fileToDownloadLocation.trim().equals("")) {
            throw new RuntimeException("The element you have specified does not link to anything!");
        }

        return new URL(fileToDownloadLocation);

    }
}

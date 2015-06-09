package org.scriptbox.selenium.ext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.scriptbox.selenium.SeleniumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpConnector implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnector.class);

    private static final String  TMP_DIR = System.getProperty("java.io.tmpdir");
    private SeleniumService service;
    private File downloads;
    private boolean followRedirects = true;
    private boolean useCookies = true;

    public HttpConnector(SeleniumService service) {
        this.service = service;
        if( TMP_DIR != null ) {
            downloads = new File( TMP_DIR );
        }
    }

    public boolean getFollowRedirects() {
        return followRedirects;
    }

    public HttpConnector setFollowRedirects(boolean value) {
        this.followRedirects = value;
        return this;
    }

    public File geDownloads() {
        return this.downloads;
    }

    public HttpConnector setDownloads(File dir) {
        this.downloads = dir;
        return this;
    }

    public boolean getUseCookies() {
        return this.useCookies;
    }

    public HttpConnector setUseCookies(boolean value) {
        this.useCookies = value;
        return this;
    }

    public HttpResponse get( String url ) throws URISyntaxException, IOException {
        return get(url, null, null);
    }

    public HttpResponse get( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        return execute( buildGet(url,params,headers) );
    }

    public HttpGet buildGet( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        LOGGER.info("buildGet: url=" + url + ", params=" + params + ", headers=" + headers );
        RequestConfig.Builder builder = buildConfig();
        HttpGet ret = new HttpGet( buildUri(url,params).build() );
        ret.setConfig( builder.build() );
        addHeaders( ret, headers );
        return ret;
    }

    public URIBuilder buildUri( String url, Map<String,String> params ) throws URISyntaxException, IOException {
        URIBuilder ub = new URIBuilder( url );
        if( params != null ) {
            for( Map.Entry<String,String> p : params.entrySet() ) {
                ub.setParameter( p.getKey(), p.getValue() );
            }
        }
        return ub;
    }

    public HttpResponse post( String url ) throws URISyntaxException, IOException {
        return post(url, null, null);
    }

    public HttpResponse post( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        return execute( buildPost(url,params,headers) );
    }

    public HttpPost buildPost( String url, Map<String,String> params, Map<String,String> headers ) throws URISyntaxException, IOException {
        LOGGER.info("buildPost: url=" + url + ", params=" + params + ", headers=" + headers );
        RequestConfig.Builder builder = buildConfig();
        HttpPost ret = new HttpPost( url );
        List<NameValuePair> nvp = new ArrayList<NameValuePair>();
        if( params != null ) {
            for( Map.Entry<String,String> p : params.entrySet() ) {
               nvp.add( new BasicNameValuePair(p.getKey(),p.getValue()) ) ;
            }
            ret.setEntity(new UrlEncodedFormEntity(nvp) );
        }

        ret.setConfig( builder.build() );
        addHeaders( ret, headers );
        return ret;
    }

    public RequestConfig.Builder buildConfig() {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setRedirectsEnabled(followRedirects);
        return builder;
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

    public void write( HttpResponse response, File file ) throws IOException {
        write(response.getEntity().getContent(), file);
    }

    public void write( InputStream stream, File file ) throws IOException {
        LOGGER.info("write: writing file: " + file );
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
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, buildCookies() );
        }
        return localContext;
    }

    public File download(WebElement element, String attribute) throws IOException, NullPointerException, URISyntaxException {
        URL url = getDownloadUrl(element, attribute );
        File file = getDownloadFile( url );

        HttpResponse response = get( url.toString() );
        int sc = response.getStatusLine().getStatusCode();
        if( sc != HttpStatus.SC_OK ) {
            throw new IOException( "Received a " + sc + " status code from: '" + url + "'" );
        }
        write( response, file );
        return file;
    }

    public File getDownloadFile( URL downloadUrl ) {
        File downloadedFile = new File(this.downloads, getDownloadFileName(downloadUrl) );
        if (downloadedFile.canWrite() == false) {
            downloadedFile.setWritable(true);
        }
        return downloadedFile;
    }

    public String getDownloadFileName( URL downloadUrl ) {
        return downloadUrl.getFile().replaceFirst("/|\\\\", "");

    }
    public URL getDownloadUrl( WebElement element, String attribute ) throws MalformedURLException {
        String location = element.getAttribute(attribute);
        if (location.trim().equals("")) {
            throw new RuntimeException("The element you have specified does not link to anything!");
        }

        return new URL(location);
    }

    private BasicCookieStore buildCookies() {
        BasicCookieStore ret = new BasicCookieStore();
        for ( Cookie seleniumCookie : service.getCookies() ) {
            BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            duplicateCookie.setDomain(seleniumCookie.getDomain());
            duplicateCookie.setSecure(seleniumCookie.isSecure());
            duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
            duplicateCookie.setPath(seleniumCookie.getPath());
            ret.addCookie(duplicateCookie);
        }
        return ret;
    }

}

package org.scriptbox.selenium.driver;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

public class Downloader implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    private String localDownloadPath = System.getProperty("java.io.tmpdir");
    private WebDriver driver;
    private boolean followRedirects = true;
    private boolean useCookies = true;
    private int httpStatus = 0;

    public Downloader( WebDriver driver ) {
        this.driver = driver;
    }

    public void setFollowRedirects(boolean value) {
        this.followRedirects = value;
    }

    public String getLocalDownloadPath() {
        return this.localDownloadPath;
    }

    public void setLocalDownloadPath(String filePath) {
        this.localDownloadPath = filePath;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public void setUseCookies(boolean value) {
        this.useCookies = value;
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

    /**
     * Perform the file/image download.
     *
     * @param element
     * @param attribute
     * @return
     * @throws IOException
     * @throws NullPointerException
     */
    public String downloader(WebElement element, String attribute) throws IOException, NullPointerException, URISyntaxException {
        String fileToDownloadLocation = element.getAttribute(attribute);
        if (fileToDownloadLocation.trim().equals("")) {
            throw new RuntimeException("The element you have specified does not link to anything!");
        }

        URL fileToDownload = new URL(fileToDownloadLocation);
        File downloadedFile = new File(this.localDownloadPath + fileToDownload.getFile().replaceFirst("/|\\\\", ""));
        if (downloadedFile.canWrite() == false) {
            downloadedFile.setWritable(true);
        }

        HttpClient client = HttpClientBuilder.create().build();
        BasicHttpContext localContext = new BasicHttpContext();

        LOGGER.info("Mimic WebDriver cookie state: " + this.useCookies);
        if (this.useCookies) {
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, mimicCookieState(this.driver.manage().getCookies()));
        }

        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setRedirectsEnabled( true );

        HttpGet httpget = new HttpGet(fileToDownload.toURI());
        httpget.setConfig( builder.build() );

        LOGGER.info("Sending GET request for: " + httpget.getURI());
        HttpResponse response = client.execute(httpget, localContext);
        this.httpStatus = response.getStatusLine().getStatusCode();
        LOGGER.info("HTTP GET request status: " + this.httpStatus);
        LOGGER.info("Downloading file: " + downloadedFile.getName());
        FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
        response.getEntity().getContent().close();

        String downloadedFileAbsolutePath = downloadedFile.getAbsolutePath();
        LOGGER.info("File downloaded to '" + downloadedFileAbsolutePath + "'");

        return downloadedFileAbsolutePath;
    }

}

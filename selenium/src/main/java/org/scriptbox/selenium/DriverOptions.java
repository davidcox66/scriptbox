package org.scriptbox.selenium;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by david on 5/18/15.
 */
public class DriverOptions implements Serializable {

    private URL url;
    private String profile;
    private boolean acceptCertificates;
    private boolean ignoreCertificateErrors;
    private File downloadDirectory;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean isAcceptCertificates() {
        return acceptCertificates;
    }

    public void setAcceptCertificates(boolean acceptCertificates) {
        this.acceptCertificates = acceptCertificates;
    }

    public boolean isIgnoreCertificateErrors() {
        return ignoreCertificateErrors;
    }

    public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
        this.ignoreCertificateErrors = ignoreCertificateErrors;
    }

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(File downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }
}

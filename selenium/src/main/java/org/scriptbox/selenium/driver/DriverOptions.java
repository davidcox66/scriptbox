package org.scriptbox.selenium.driver;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

/**
 * Created by david on 5/18/15.
 */
public class DriverOptions implements Serializable {

    private URL url;
    private String profile;
    private boolean acceptCertificates;
    private boolean ignoreCertificateErrors;
    private File downloadDirectory;
    private List<File> extensions;

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

    public List<File> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<File> extensions) {
        this.extensions = extensions;
    }
}

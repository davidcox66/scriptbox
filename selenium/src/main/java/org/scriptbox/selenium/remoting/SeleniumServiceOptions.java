package org.scriptbox.selenium.remoting;

import java.io.File;
import java.io.Serializable;

/**
 * Created by david on 5/29/15.
 */
public class SeleniumServiceOptions implements Serializable {

    private File downloadDirectory;

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    public void setDownloadDirectory(File downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
    }
}

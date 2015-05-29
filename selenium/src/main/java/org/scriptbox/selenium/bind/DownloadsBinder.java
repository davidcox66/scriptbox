package org.scriptbox.selenium.bind;

import groovy.lang.Binding;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by david on 5/28/15.
 */
public class DownloadsBinder implements Bindable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadsBinder.class);

    private File downloads;

    public DownloadsBinder( File downloads ) {
        this.downloads = downloads;
    }

    @Override
    public void bind(Binding binding) {
        BindUtils.bind(binding, this, "getLatestDownload");
        BindUtils.bind( binding, this, "getDownloads" );
        BindUtils.bind( binding, this, "purge" );

    }

    public File[] getDownloads() {
        File[] ret = downloads.listFiles();
        LOGGER.debug( "getDownloads: dir=" + downloads + ", files=" + ret );
        return ret;
    }

    public File getLatestDownload() {
        return getLatestDownload( 10, 2 );
    }

    public File getLatestDownload( int wait, int quiet ) {
        if( waitForAnyFileToAppear(wait) ) {
            File ret = waitForQuiet( quiet );
            LOGGER.debug( "getLatestDownload: dir=" + downloads + ", latest=" + ret );
            return ret;
        }
        else {
            throw new RuntimeException( "Timed-out waiting for a file download to appear" );
        }
    }

    public void purge() {
        LOGGER.debug( "purge: cleaning downloads directory - dir=" + downloads );
        for( File file : downloads.listFiles() ) {
            LOGGER.debug( "purge: removing file=" + file );
            file.delete();
        }
    }

    private File waitForQuiet( int seconds ) {
        File latest = getLatestFile();
        File file = latest;
        LOGGER.debug( "waitForQuiet: waiting for '" + file + "' to go quiet for " + seconds + " seconds" );
        long last = file.lastModified();
        while( System.currentTimeMillis() - last < seconds * 1000 ) {
            sleep( (seconds * 1000) - (System.currentTimeMillis() - last) );
            latest = getLatestFile();
            if( !latest.equals(file) ) {
                LOGGER.debug( "waitForQuiet: latest file changed from '" + file + "' to '" + latest + "'" );
                file = latest;
            }
            last = file.lastModified();
        }
        LOGGER.debug( "waitForQuiet: finished waiting quiet on '" + file + "'");
        return file;

    }

    private boolean waitForAnyFileToAppear( int seconds ) {
        LOGGER.debug( "waitForAnyFileToAppear: waiting for " + seconds + " seconds for a file to appear in '" + downloads + "'");
        long timeout = System.currentTimeMillis() + (seconds * 1000);
        boolean found = false;
        do {
            found = isAnyFiles();
            if( !found ) {
                sleep( 1000 );
            }
        }
        while( System.currentTimeMillis() < timeout && !found );
        LOGGER.debug( "waitForAnyFileToAppear: finished waiting file to appear - found=" + found );
        return found;
    }

    private void sleep( long millis ) {
        try {
            Thread.sleep( millis );
        }
        catch( Exception ex ) {
            LOGGER.error( "Error while sleeping", ex );
        }
    }

    private boolean isAnyFiles() {
        return downloads.listFiles().length > 0;
    }

    private File getLatestFile() {
        File[] files = downloads.listFiles();
        File ret = null;
        if( files.length > 0 ) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return (int) (f2.lastModified() - f1.lastModified());
                }
            });
            ret = files[0];
        }
        return ret;
    }
}

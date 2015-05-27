package org.scriptbox.selenium;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 5/18/15.
 */
public class Selenium {

    private static final Logger LOGGER = LoggerFactory.getLogger(Selenium.class);

    public static void sleep( int millis ) {
        LOGGER.debug( "waiting " + millis + " milliseconds");
        try {
            Thread.sleep( millis );
        }
        catch( InterruptedException ex ) {
            return;
        }
    }

    public static void pause( int seconds ) {
        sleep( seconds * 1000 );
    }

}

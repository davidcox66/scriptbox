package org.scriptbox.selenium.driver;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 6/4/15.
 */
public class ServiceHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceHelper.class);

    private static final int RETRY_SECONDS = 1;

    public static boolean isRetryableException( WebDriverException ex ) {
        return (ex.getMessage() != null && ex.getMessage().indexOf( "Element is not clickable") >= 0) ||
                ex instanceof StaleElementReferenceException;
    }

    public static <X> X actionWithRetry( final int seconds, RetryableAction<X> action ) {
        Exception last = null;
        long timeout = System.currentTimeMillis() + (seconds * 1000);
        while( System.currentTimeMillis() <= timeout ) {
            try {
                int sec = (int)(timeout - System.currentTimeMillis())  / 1000;
                if( sec > 0 ) {
                    return action.run(sec);
                }
            }
            catch( WebDriverException ex ) {
                if( !isRetryableException(ex) ) {
                    throw ex;
                }
                last = ex;
                LOGGER.debug( "actionWithRetry: retrying...", ex );
                try {
                    Thread.sleep( RETRY_SECONDS * 1000 );
                }
                catch( InterruptedException ex2 ) {
                    LOGGER.debug( "actionWithRetry: interrupted", ex2 );
                }
            }
            catch( Exception ex ) {
                LOGGER.error( "actionWithRetry: unexpected exception", ex );
            }
        }
        throw new RuntimeException( "Unable to complete action within " + seconds + " seconds" );
    }


}

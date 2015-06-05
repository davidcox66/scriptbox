package org.scriptbox.selenium.driver;

/**
 * Created by david on 6/4/15.
 */
public interface RetryableAction<X> {
    X run(int seconds);
}

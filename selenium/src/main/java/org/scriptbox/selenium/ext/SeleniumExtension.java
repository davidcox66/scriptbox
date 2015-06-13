package org.scriptbox.selenium.ext;

/**
 * Created by david on 5/29/15.
 */
public interface SeleniumExtension {

    public void init( SeleniumExtensionContext context );
    public void configure() throws Exception;
    public void run() throws Exception;
}

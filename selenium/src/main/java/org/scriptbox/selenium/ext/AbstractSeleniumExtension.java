package org.scriptbox.selenium.ext;

/**
 * Created by david on 6/12/15.
 */
public class AbstractSeleniumExtension implements SeleniumExtension {

    protected SeleniumExtensionContext context;

    public void init( SeleniumExtensionContext context ) {
        this.context = context;
    }

    public void configure() throws Exception { }

    public void run() throws Exception { }
}

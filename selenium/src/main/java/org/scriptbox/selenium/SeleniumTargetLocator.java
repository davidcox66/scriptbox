package org.scriptbox.selenium;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Created by david on 5/21/15.
 */
public class SeleniumTargetLocator implements WebDriver.TargetLocator {

    private SeleniumService service;

    public SeleniumTargetLocator( SeleniumService service ) {
        this.service = service;
    }

    @Override
    public WebDriver frame(int index) {
        service.switchToFrameByIndex( index );
        return null;
    }

    @Override
    public WebDriver frame(String nameOrId) {
        service.switchToFrameByNameOrId( nameOrId );
        return null;
    }

    @Override
    public WebDriver frame(WebElement frameElement) {
        service.switchToFrameByElement( frameElement );
        return null;
    }

    @Override
    public WebDriver window(String nameOrHandle) {
        service.switchToWindow( nameOrHandle );
        return null;
    }

    @Override
    public WebDriver defaultContent() {
        service.switchToDefaultContent();
        return null;
    }

    @Override
    public WebElement activeElement() {
        return service.switchToActiveElement();
    }

    @Override
    public Alert alert() {
        return null;
    }
}

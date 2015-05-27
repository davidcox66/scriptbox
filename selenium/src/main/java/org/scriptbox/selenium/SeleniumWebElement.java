package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import java.io.Serializable;
import java.util.List;

/**
 * Created by david on 5/26/15.
 */
public class SeleniumWebElement implements WebElement, Serializable {

    private String id;
    private transient SeleniumService service;

    public SeleniumWebElement() {
    }

    public SeleniumWebElement( String id, SeleniumService service ) {
        this.id = id;
        this.service = service;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SeleniumService getService() {
        return service;
    }

    public void setService(SeleniumService service) {
        this.service = service;
    }

    @Override
    public void click() {
        service.click(this);
    }

    @Override
    public void submit() {
        service.submit(this);

    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        service.sendKeys(this, keysToSend);
    }

    @Override
    public void clear() {
        service.clear(this);

    }

    @Override
    public String getTagName() {
        return service.getTagName(this);
    }

    @Override
    public String getAttribute(String name) {
        return service.getAttribute(this, name);
    }

    @Override
    public boolean isSelected() {
        return service.isSelected(this);
    }

    @Override
    public boolean isEnabled() {
        return service.isEnabled(this);
    }

    @Override
    public String getText() {
        return service.getText(this);
    }

    @Override
    public List<WebElement> findElements(By by) {
        return service.findElements(this, by);
    }

    @Override
    public WebElement findElement(By by) {
        return service.findElement(this, by);
    }

    @Override
    public boolean isDisplayed() {
        return service.isDisplayed( this );
    }

    @Override
    public Point getLocation() {
        return service.getLocation( this );
    }

    @Override
    public Dimension getSize() {
        return service.getSize( this );
    }

    @Override
    public String getCssValue(String propertyName) {
        return service.getCssValue( this, propertyName );
    }
}

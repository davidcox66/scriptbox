package org.scriptbox.selenium.remoting;

import groovy.lang.GroovyObjectSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.scriptbox.selenium.Bys;
import org.scriptbox.selenium.SeleniumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by david on 5/26/15.
 */
public class SeleniumWebElement extends GroovyObjectSupport implements WebElement, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumWebElement.class);

    private String id;
    private By by;
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
        return service.findElements(this, by, 0);
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

    public By getBy() {
        return by;
    }

    public void setBy(By by) {
        this.by = by;
    }

    public Object propertyMissing(String name) {
        Object ret =  getAttribute( name );
        if( ret == null ) {
            LOGGER.warn( "propertyMissing: attribute '" + name + "' not found");
        }
        return ret;
    }

    public Object methodMissing(String name, Object[] args) {
        Object ret =  getAttribute( name );
        if( ret == null ) {
            LOGGER.warn( "methodMissing: attribute '" + name + "' not found");
        }
        return ret;
    }

    public WebElement get( String str ) {
        return get( Bys.byGuess(str) ) ;
    }
    public WebElement get( By by) {
        return service.findElement( this, by );
    }

    public List<WebElement> getAll( String str ) {
        return getAll( Bys.byGuess(str) );
    }

    public List<WebElement> getAll( By by ) {
        return service.findElements( this, by, 0 );
    }

    public WebElement leftShift( CharSequence val ) {
        sendKeys( val );
        return this;
    }
}

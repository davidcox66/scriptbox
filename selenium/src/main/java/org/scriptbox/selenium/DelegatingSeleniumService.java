package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by david on 5/27/15.
 */
public class DelegatingSeleniumService implements SeleniumService {

    protected SeleniumService delegate;

    public DelegatingSeleniumService() {
    }

    public DelegatingSeleniumService( SeleniumService delegate ) {
        this.delegate = delegate;
    }

    @Override
    public boolean activate( ExpectedCondition<Boolean> cond ) {
        return delegate.activate( cond );
    }

    @Override
    public void connect() {
        delegate.connect();
    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public void quit() {
        delegate.quit();
    }

    @Override
    public void get(String url) {
        delegate.get( url );
    }

    @Override
    public void execute(String script, Object... args) {
        delegate.execute( script, args );
    }

    @Override
    public void executeAsync(String script, Object... args) {
        delegate.executeAsync( script, args );
    }

    @Override
    public boolean isElementExists(By by) {
        return delegate.isElementExists( by );
    }

    @Override
    public WebElement getElement(By by) {
        return delegate.getElement( by );
    }

    @Override
    public List<WebElement> getElements(By by) {
        return delegate.getElements( by );
    }

    @Override
    public WebElement waitForElement(By by, int seconds) {
        return delegate.waitForElement( by, seconds );
    }

    @Override
    public String waitForNewWindow(int seconds) {
        return delegate.waitForNewWindow( seconds );
    }

    @Override
    public <X> X waitFor(int seconds, ExpectedCondition<X> cond) {
        return delegate.waitFor( seconds, cond );
    }

    @Override
    public WebElement clickElement(By by, int seconds) {
        return delegate.clickElement( by, seconds );
    }

    @Override
    public WebElement moveToBy(By by, int seconds) {
        return delegate.moveToBy( by, seconds );
    }

    @Override
    public WebElement moveToElement(WebElement element, int seconds) {
        return delegate.moveToElement( element, seconds );
    }

    @Override
    public void mouseDown(WebElement element) {
        delegate.mouseDown( element );
    }

    @Override
    public void mouseUp(WebElement element) {
        delegate.mouseUp( element );
    }

    @Override
    public void mouseMove(WebElement element) {
        delegate.mouseMove( element );
    }

    @Override
    public void mouseClick(WebElement element) {
        delegate.mouseClick( element );
    }

    @Override
    public void mouseDoubleClick(WebElement element) {
        delegate.mouseDoubleClick( element );
    }

    @Override
    public void mouseContextClick(WebElement element) {
        delegate.mouseContextClick( element );
    }

    @Override
    public void screenshot(File file) throws IOException {
        delegate.screenshot( file );
    }

    @Override
    public void click(WebElement element) {
        delegate.click( element );
    }

    @Override
    public void submit(WebElement element) {
        delegate.submit( element );
    }

    @Override
    public void sendKeys(WebElement element, CharSequence... keysToSend) {
        delegate.sendKeys( element, keysToSend );
    }

    @Override
    public void clear(WebElement element) {
        delegate.clear( element );
    }

    @Override
    public String getTagName(WebElement element) {
        return delegate.getTagName( element );
    }

    @Override
    public String getAttribute(WebElement element, String name) {
        return delegate.getAttribute( element, name );
    }

    @Override
    public String getText(WebElement element) {
        return delegate.getText( element );
    }

    @Override
    public List<WebElement> findElements(WebElement element, By by) {
        return delegate.findElements( element, by );
    }

    @Override
    public WebElement findElement(WebElement element, By by) {
        return delegate.findElement( element, by );
    }

    @Override
    public String getCssValue(WebElement element, String propertyName) {
        return delegate.getCssValue( element, propertyName );
    }

    @Override
    public boolean isDisplayed(WebElement element) {
        return delegate.isDisplayed( element );
    }

    @Override
    public boolean isSelected(WebElement element) {
        return delegate.isSelected( element );
    }

    @Override
    public boolean isEnabled(WebElement element) {
        return delegate.isEnabled( element );
    }

    @Override
    public Point getLocation(WebElement element) {
        return delegate.getLocation( element );
    }

    @Override
    public Dimension getSize(WebElement element) {
        return delegate.getSize( element );
    }

    @Override
    public String getCurrentUrl() {
        return delegate.getCurrentUrl();
    }

    @Override
    public String getWindowHandle() {
        return delegate.getWindowHandle();
    }

    @Override
    public void closeWindow() {
        delegate.closeWindow();
    }

    @Override
    public void switchToFrameByIndex(int index) {
        delegate.switchToFrameByIndex( index );
    }

    @Override
    public void switchToFrameByNameOrId(String nameOrId) {
        delegate.switchToFrameByNameOrId( nameOrId );
    }

    @Override
    public void switchToFrameByElement(WebElement element) {
        delegate.switchToFrameByElement( element );
    }

    @Override
    public void switchToWindow(String nameOrHandle) {
        delegate.switchToWindow( nameOrHandle );
    }

    @Override
    public void switchToDefaultContent() {
        delegate.switchToDefaultContent();
    }

    @Override
    public WebElement switchToActiveElement() {
        return delegate.switchToActiveElement();
    }

    @Override
    public void back() {
        delegate.back();
    }

    @Override
    public void forward() {
        delegate.forward();
    }

    @Override
    public void to(URL url) {
        delegate.to( url );
    }

    @Override
    public void refresh() {
        delegate.refresh();
    }
}

package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by david on 5/27/15.
 */
public class ServerSeleniumService extends DelegatingSeleniumService {

    private Map<String,WebElement> elementsById = new WeakHashMap<String,WebElement>();

    public ServerSeleniumService() {
        super( new DriverSeleniumService() );
    }

    public ServerSeleniumService( SeleniumService delegate ) {
        super( delegate );
    }

    @Override
    public WebElement getElement(By by) {
        return toSerializable(super.getElement(by));
    }

    @Override
    public List<WebElement> getElements(By by) {
        return toSerializable(super.getElements(by));
    }

    @Override
    public WebElement waitForElement(By by, int seconds) {
        return toSerializable(super.waitForElement(by, seconds));
    }

    @Override
    public WebElement clickElement(By by, int seconds) {
        return toSerializable(super.clickElement(by, seconds));
    }

    @Override
    public WebElement moveToBy(By by, int seconds) {
        return toSerializable(super.moveToBy(by, seconds));
    }

    @Override
    public WebElement moveToElement(WebElement element, int seconds) {
        return toSerializable(super.moveToElement(toLocal(element), seconds));
    }

    @Override
    public <X> X waitFor(final int seconds, final ExpectedCondition<X> cond) {
        Object ret = super.waitFor( seconds, cond ) ;
        if( ret != null && ret instanceof RemoteWebElement ) {
            ret = toSerializable( (WebElement)ret );
        }
        return (X)ret;
    }

    @Override
    public void mouseDown(WebElement element) {
        super.mouseDown(toLocal(element));
    }

    @Override
    public void mouseUp(WebElement element) {
        super.mouseUp(toLocal(element));
    }

    @Override
    public void mouseMove(WebElement element) {
        super.mouseMove(toLocal(element));
    }

    @Override
    public void mouseClick(WebElement element) {
        super.mouseClick(toLocal(element));
    }

    @Override
    public void mouseDoubleClick(WebElement element) {
        super.mouseDoubleClick(toLocal(element));
    }

    @Override
    public void mouseContextClick(WebElement element) {
        super.mouseContextClick(toLocal(element));
    }

    @Override
    public void click(WebElement element) {
        super.click(toLocal(element));
    }

    @Override
    public void submit(WebElement element) {
        super.submit(toLocal(element));
    }

    @Override
    public void sendKeys(WebElement element, CharSequence... keysToSend) {
        super.sendKeys(toLocal(element), keysToSend);
    }

    @Override
    public void clear(WebElement element) {
        super.clear(toLocal(element));
    }

    @Override
    public String getTagName(WebElement element) {
        return super.getTagName(toLocal(element));
    }

    @Override
    public String getAttribute(WebElement element, String name) {
        return super.getAttribute(toLocal(element), name);
    }

    @Override
    public String getText(WebElement element) {
        return super.getText(toLocal(element));
    }

    @Override
    public List<WebElement> findElements(WebElement element, By by) {
        return toSerializable(super.findElements(toLocal(element), by));
    }

    @Override
    public WebElement findElement(WebElement element, By by) {
        return super.findElement(toLocal(element), by);
    }

    @Override
    public String getCssValue(WebElement element, String propertyName) {
        return super.getCssValue(toLocal(element), propertyName);
    }

    @Override
    public boolean isDisplayed(WebElement element) {
        return super.isDisplayed(toLocal(element));
    }

    @Override
    public boolean isSelected(WebElement element) {
        return super.isSelected(toLocal(element));
    }

    @Override
    public boolean isEnabled(WebElement element) {
        return super.isEnabled(toLocal(element));
    }

    @Override
    public Point getLocation(WebElement element) {
        return super.getLocation(toLocal(element));
    }

    @Override
    public Dimension getSize(WebElement element) {
        return super.getSize(toLocal(element));
    }

    @Override
    public void switchToFrameByElement(WebElement element) {
        super.switchToFrameByElement(toLocal(element));
    }

    @Override
    public WebElement switchToActiveElement() {
        return toSerializable(super.switchToActiveElement());
    }

    private List<WebElement> toLocal( List<WebElement> elements ) {
        if( elements != null ) {
            List<WebElement> ret = new ArrayList<WebElement>(elements.size());
            for (WebElement el : elements) {
                ret.add(toLocal(el));
            }
            return ret;
        }
        return null;
    }

    private WebElement toLocal( WebElement element ) {
        WebElement ret = null;
        if( element != null ) {
            if( element instanceof SeleniumWebElement ) {
                SeleniumWebElement sel = (SeleniumWebElement)element;
                String id = sel.getId();
                ret = elementsById.get( id );
                if( ret == null ) {
                    ret = delegate.getElement(By.id(id));
                }
            }
            else if( element instanceof RemoteWebElement) {
                ret = element;
            }
            else {
                throw new RuntimeException( "Unknown web element type: " + element );
            }
        }
        return ret;
    }

    private List<WebElement> toSerializable( List<WebElement> elements ) {
        if( elements != null ) {
            List<WebElement> ret = new ArrayList<WebElement>(elements.size());
            for (WebElement el : elements) {
                ret.add(toSerializable(el));
            }
            return ret;
        }
        return null;
    }

    private WebElement toSerializable( WebElement element ) {
        WebElement ret = null;
        if( element != null ) {
            if( element instanceof SeleniumWebElement ) {
                ret = element;
            }
            else if( element instanceof RemoteWebElement) {
                RemoteWebElement rel = (RemoteWebElement)element;
                String id = rel.getId();
                ret = new SeleniumWebElement(id, this );
                elementsById.put( id, rel );
            }
            else {
                throw new RuntimeException( "Unknown web element type: " + element );
            }
        }
        return ret;
    }

}

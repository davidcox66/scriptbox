package org.scriptbox.selenium.remoting;

import org.eclipse.jetty.server.Server;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.scriptbox.selenium.DelegatingSeleniumService;
import org.scriptbox.selenium.SeleniumService;
import org.scriptbox.selenium.driver.DriverSeleniumService;
import org.scriptbox.selenium.driver.RetryableAction;
import org.scriptbox.selenium.driver.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by david on 5/27/15.
 */
public class ServerSeleniumService extends DelegatingSeleniumService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerSeleniumService.class);

    private Map<String,WebElement> elementsById = new WeakHashMap<String,WebElement>();

    public ServerSeleniumService() {
        super( new DriverSeleniumService() );
    }

    public ServerSeleniumService( SeleniumService delegate ) {
        super( delegate );
    }


    @Override
    public Object execute(String script, Object... args) {
        Object ret = super.execute(script, args);
        if( ret != null && ret instanceof WebElement ) {
            ret = toSerializable( (WebElement)ret );
        }
        return ret;
    }

    @Override
    public boolean activate( ExpectedCondition<Boolean> cond ) {
        return super.activate( toLocal(cond) );
    }

    @Override
    public WebElement getElement(By by) {
        return toSerializable(super.getElement(by), by);
    }

    @Override
    public List<WebElement> getElements(By by) {
        return toSerializable(super.getElements(by));
    }

    @Override
    public WebElement waitForElement(By by, int seconds) {
        return toSerializable(super.waitForElement(by, seconds), by);
    }

    @Override
    public WebElement clickElement(By by, int seconds) {
        return toSerializable(super.clickElement(by, seconds), by);
    }

    @Override
    public WebElement moveToBy(By by, int seconds) {
        return toSerializable(super.moveToBy(by, seconds), by);
    }

    @Override
    public WebElement moveToElement(WebElement element, int seconds) {
        return toSerializable(super.moveToElement(toLocal(element), seconds));
    }

    @Override
    public <X> X waitFor(final int seconds, ExpectedCondition<X> cond) {
        cond = toLocal(cond);
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
    public List<WebElement> findElements( final WebElement element, final By by, final int seconds ) {
        class Holder
        {
            public WebElement element;
        }
        final Holder holder = new Holder();
        holder.element = toLocal(element);
        return ServiceHelper.actionWithRetry(30, new RetryableAction<List<WebElement>>() {
            public List<WebElement> run(int seconds) {
                try {
                    return toSerializable( ServerSeleniumService.super.findElements(holder.element, by, seconds) );
                }
                catch (WebDriverException ex) {
                    if (ServiceHelper.isRetryableException(ex)) {
                        SeleniumWebElement sel = (SeleniumWebElement)element;
                        elementsById.remove( sel.getId() );
                        if( sel.getBy() != null ) {
                            LOGGER.debug("findElements: refetching element by='" + sel.getBy() + "'");
                            holder.element = ServerSeleniumService.super.getElement(sel.getBy());
                            addToCache( holder.element );
                        }
                    }
                    throw ex;
                }
            }
        });
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

    public List<WebElement> toLocal( List<WebElement> elements ) {
        if( elements != null ) {
            List<WebElement> ret = new ArrayList<WebElement>(elements.size());
            for (WebElement el : elements) {
                ret.add(toLocal(el));
            }
            return ret;
        }
        return null;
    }

    public WebElement toLocal( WebElement element ) {
        WebElement ret = null;
        if( element != null ) {
            if( element instanceof SeleniumWebElement) {
                SeleniumWebElement sel = (SeleniumWebElement)element;
                String id = sel.getId();
                ret = elementsById.get( id );
                if( ret == null ) {
                    ret = delegate.getElement(By.id(id));
                }
                if( sel.getBy() != null ) {
                    ret = delegate.getElement(sel.getBy());
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

    public <X> ExpectedCondition<X> toLocal( ExpectedCondition<X> cond ) {
        if( cond != null && cond instanceof RemotableCondition) {
            return ((RemotableCondition)cond).getLocal(this);
        }
        return cond;
    }

    public List<WebElement> toSerializable( List<WebElement> elements ) {
        if( elements != null ) {
            List<WebElement> ret = new ArrayList<WebElement>(elements.size());
            for (WebElement el : elements) {
                ret.add(toSerializable(el));
            }
            return ret;
        }
        return null;
    }

    public WebElement toSerializable( WebElement element ) {
        return toSerializable( element, null );
    }

    public WebElement toSerializable( WebElement element, By by ) {
        WebElement ret = null;
        if( element != null ) {
            if( element instanceof SeleniumWebElement ) {
                ret = element;
            }
            else if( element instanceof RemoteWebElement) {
                SeleniumWebElement sel = new SeleniumWebElement(addToCache(element), this );
                if( by != null ) {
                    sel.setBy( by );
                }
                ret = sel;
            }
            else {
                throw new RuntimeException( "Unknown web element type: " + element );
            }
        }
        return ret;
    }

    private String addToCache( WebElement element ) {
        RemoteWebElement rel = (RemoteWebElement)element;
        String id = rel.getId();
        elementsById.put( id, rel );
        return id;
    }
}

package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 5/27/15.
 */
public class ClientSeleniumService extends DelegatingSeleniumService {

    public ClientSeleniumService() {
    }

    public ClientSeleniumService(SeleniumService delegate) {
        super(delegate);
    }

    @Override
    public WebElement getElement(By by) {
        return toLocal( super.getElement(by) );
    }

    @Override
    public List<WebElement> getElements(By by) {
        return toLocal(super.getElements(by));
    }

    @Override
    public WebElement waitForElement(By by, int seconds) {
        return toLocal(super.waitForElement(by, seconds));
    }

    @Override
    public WebElement clickElement(By by, int seconds) {
        return toLocal(super.clickElement(by, seconds));
    }

    @Override
    public WebElement moveToBy(By by, int seconds) {
        return toLocal(super.moveToBy(by, seconds));
    }

    @Override
    public WebElement moveToElement(WebElement element, int seconds) {
        return toLocal(super.moveToElement(element, seconds));
    }

    @Override
    public List<WebElement> findElements(WebElement element, By by) {
        return toLocal(super.findElements(element, by));
    }

    @Override
    public WebElement findElement(WebElement element, By by) {
        return toLocal(super.findElement(element, by));
    }

    @Override
    public <X> X waitFor(final int seconds, final ExpectedCondition<X> cond) {
        Object ret = super.waitFor( seconds, cond ) ;
        if( ret != null && ret instanceof WebElement) {
            ret = toLocal( (WebElement)ret );
        }
        return (X)ret;
    }

    @Override
    public WebElement switchToActiveElement() {
        return toLocal(super.switchToActiveElement());
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
        SeleniumWebElement ret = (SeleniumWebElement)element;
        ret.setService(this);
        return ret;
    }

}

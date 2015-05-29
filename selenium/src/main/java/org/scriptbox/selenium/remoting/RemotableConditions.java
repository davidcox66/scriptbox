package org.scriptbox.selenium.remoting;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by david on 5/19/15.
 */
public class RemotableConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotableConditions.class);

    public static abstract class ByCondition<X,P> extends RemotableCondition<X> {
        protected By by;
        protected P param;

        public ByCondition( By by )  {
            this.by = by;
        }
        public ByCondition( By by, P param ) {
            this.by = by;
            this.param = param;
        }
    }

    public static abstract class WebElementCondition<X,P> extends RemotableCondition<X> {
        protected WebElement element;
        protected P param;

        public WebElementCondition( WebElement element )  {
            this.element = element;
        }
        public WebElementCondition( WebElement element, P param ) {
            this.element = element;
            this.param = param;
        }
    }

    public static class PresenceOf extends ByCondition<WebElement,Object> {

        public PresenceOf( By by ) {

            super( by );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return ExpectedConditions.presenceOfElementLocated(by);
        }
    }
    public static class PresenceOfAll extends ByCondition<List<WebElement>,Object> {

        public PresenceOfAll( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<List<WebElement>> localize( ServerSeleniumService service ) {
            return ExpectedConditions.presenceOfAllElementsLocatedBy(by);
        }
    }

    public static class VisibilityOf extends ByCondition<WebElement,Object> {

        public VisibilityOf( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return ExpectedConditions.visibilityOfElementLocated(by);
        }
    }

    public static class VisibilityOfAll extends ByCondition<List<WebElement>,Object> {

        public VisibilityOfAll( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<List<WebElement>> localize( ServerSeleniumService service ) {
            return ExpectedConditions.visibilityOfAllElementsLocatedBy(by);
        }
    }

    public static class VisibilityOfElement extends WebElementCondition<WebElement,Object> {

        public VisibilityOfElement( WebElement element ) {
            super( element );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return ExpectedConditions.visibilityOf(service.toLocal(element));
        }
    }

    public static class InvisibilityOf extends ByCondition<Boolean,Object> {

        public InvisibilityOf( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.invisibilityOfElementLocated(by);
        }
    }

    public static class InvisibilityOfWithText extends ByCondition<Boolean,String> {

        public InvisibilityOfWithText( By by, String text ) {
            super( by, text );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.invisibilityOfElementWithText(by, param);
        }
    }

    public static class InvisibilityOfElement extends WebElementCondition<Boolean,Object> {

        public InvisibilityOfElement( WebElement element ) {
            super( element );
        }
        public Boolean apply(WebDriver driver) {
            try {
                return !element.isDisplayed();
            }
            catch (NoSuchElementException e) {
                // Returns true because the element is not present in DOM. The
                // try block checks if the element is present but is invisible.
                return true;
            }
            catch (StaleElementReferenceException e) {
                // Returns true because stale element reference implies that element
                // is no longer visible.
                return true;
            }
        }
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            element = service.toLocal(element);
            return super.localize( service );
        }
    }

    public static class InvisibilityOfAll implements ExpectedCondition<Boolean>, Serializable {

        private By by;

        public InvisibilityOfAll( By by ) {
            this.by = by;
        }
        public Boolean apply(WebDriver driver) {
                List<WebElement> elements = driver.findElements(by);
                for(WebElement element : elements){
                    try {
                        if (element.isDisplayed()) {
                            return false;
                        }
                    }
                    catch (NoSuchElementException e) {
                        // Returns true because the element is not present in DOM. The
                        // try block checks if the element is present but is invisible.
                    }
                    catch (StaleElementReferenceException e) {
                        // Returns true because stale element reference implies that element
                        // is no longer visible.
                    }
            }
            return true;
        }
    }

    public static class TextPresent extends ByCondition<Boolean,String> {

        public TextPresent( By by, String text ) {
            super( by, text );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.textToBePresentInElementLocated(by, param);
        }
    }

    public static class TextPresentInElement extends WebElementCondition<Boolean,String> {

        public TextPresentInElement( WebElement element, String text ) {
            super( element, text );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.textToBePresentInElement(service.toLocal(element), param);
        }
    }

    public static class ValuePresent extends ByCondition<Boolean,String> {

        public ValuePresent( By by, String text ) {
            super( by, text );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.textToBePresentInElementValue(by, param);
        }
    }

    public static class ValuePresentInElement extends WebElementCondition<Boolean,String> {

        public ValuePresentInElement( WebElement element, String text ) {
            super( element, text );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.textToBePresentInElementValue(service.toLocal(element), param);
        }
    }

    public static class Selected extends ByCondition<Boolean,Boolean> {

        public Selected( By by, Boolean sel ) {
            super( by, sel );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.elementSelectionStateToBe(by, param);
        }
    }

    public static class SelectedElement extends WebElementCondition<Boolean,Object> {

        public SelectedElement( WebElement element ) {
            super( element );
        }
        @Override
        public ExpectedCondition<Boolean> localize( ServerSeleniumService service ) {
            return ExpectedConditions.elementToBeSelected(service.toLocal(element));
        }
    }

    public static class Clickable extends ByCondition<WebElement,Object> {

        public Clickable( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return ExpectedConditions.elementToBeClickable(by);
        }
    }

    public static class ClickableElement extends WebElementCondition<WebElement,Object> {

        public ClickableElement( WebElement element ) {
            super( element );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return ExpectedConditions.elementToBeClickable(service.toLocal(element));
        }
    }

    public static class ClickableAll extends ByCondition<List<WebElement>,Object> {

        public ClickableAll( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<List<WebElement>> localize( ServerSeleniumService service ) {
            return new ExpectedCondition<List<WebElement>>() {
                public List<WebElement> apply(WebDriver driver) {
                    try {
                        List<WebElement> ret = new ArrayList<WebElement>();
                        List<WebElement> elements = driver.findElements( by );
                        for(WebElement element : elements){
                            if( element.isDisplayed() ) {
                                ret.add( element );
                            }
                        }
                        return ret.size() > 0 ? ret : null;
                    }
                    catch( RuntimeException ex ) {
                        LOGGER.error( "clickableAll: error evaluating: " + by, ex );
                        throw ex;
                    }
                }

                public String toString() {
                    return "all clickable {" + by + "}";
                }
            };
        }
    }

    public static class ClickableAny extends ByCondition<WebElement,Object> {

        public ClickableAny( By by ) {
            super( by );
        }
        @Override
        public ExpectedCondition<WebElement> localize( ServerSeleniumService service ) {
            return new ExpectedCondition<WebElement>() {
                public WebElement apply(WebDriver driver) {
                    try {
                        List<WebElement> elements = driver.findElements( by );
                        for(WebElement element : elements){
                            if( element.isDisplayed() ) {
                                return element;
                            }
                        }
                        return null;
                    }
                    catch( RuntimeException ex ) {
                        LOGGER.error( "clickableAny: error evaluating: " + by, ex );
                        throw ex;
                    }
                }

                public String toString() {
                    return "any clickable {" + by + "}";
                }
            };
        }
    }

    public static class AtBaseUrl implements ExpectedCondition<Boolean>, Serializable {
        private String url;

        public AtBaseUrl( String url ) {
            this.url = url;
        }

        public Boolean apply(WebDriver driver) {
            String current = driver.getCurrentUrl();
            LOGGER.debug("AtBaseUrl.apply: base: " + url + ", current: " + current);
            return current != null && current.startsWith(url);
        }
    }

    public static class AtUrlPattern implements ExpectedCondition<Boolean>, Serializable {
        private Pattern pattern;

        public AtUrlPattern( Pattern pattern ) {
            this.pattern = pattern;
        }

        public Boolean apply(WebDriver driver) {
            String current = driver.getCurrentUrl();
            LOGGER.debug("isAtLocation: pattern: " + pattern + ", current: " + current);
            return current != null && pattern.matcher(current).matches();
        }
    }

}

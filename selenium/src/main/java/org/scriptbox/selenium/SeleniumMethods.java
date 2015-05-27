package org.scriptbox.selenium;

import groovy.lang.Closure;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SeleniumMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger( SeleniumMethods.class );
	private static final int DEFAULT_WAIT = 30;

	protected int wait = DEFAULT_WAIT;
    protected SeleniumService service;

    public SeleniumMethods(SeleniumService delegate)  {
        this.service = delegate;
    }

	public int getWait() {
		return wait;
	}

	public void setWait(int wait) {
		this.wait = wait;
	}

	public boolean activate( ExpectedCondition<Boolean> cond ) {
		return service.activate( cond );
	}

	public ExpectedCondition<Boolean> baseUrl( String url ) {
		return new RemotableConditions.AtBaseUrl(url);
	}

	public ExpectedCondition<Boolean> urlPattern( Pattern pattern ) {
		return new RemotableConditions.AtUrlPattern(pattern);
	}

	public void connect() {
    	service.connect();
    }

	public void disconnect() {
    	service.disconnect();
    }
	public void quit() {
    	service.quit();
    }
    public void sleep( int millis ) {
		LOGGER.debug( "waiting " + millis + " milliseconds");
		try {
			Thread.sleep( millis );
		}
		catch( InterruptedException ex ) {
			return;
		}
    }
    public void pause( int seconds ) {
		sleep( seconds * 1000 );
    }
    
	public void get(String url) {
    	service.get(url);
    }
    
	public void execute(String script, Object... args) {
		service.execute(script, args);
    }

	public void executeAsync(String script, Object... args) {
		service.execute(script, args);
    }

	public boolean isElementExistsById(String id) {
        return isElementExists( byId(id) );
    }
	public boolean isElementExistsByName(String name) {
        return isElementExists( byName(name) );
    }
	public boolean isElementExistsByXpath(String xpath) {
        return isElementExists( byXpath(xpath) );
    }
	public boolean isElementExistsByCss(String css) {
        return isElementExists(byCss(css));
    }
    
	public boolean isElementExists(By by) {
		return service.isElementExists(by);
    }
    
	public WebElement getElementById(final String id) {
    	return getElement(byId(id));
    }
	public WebElement getElementByName(final String name) {
    	return getElement(byName(name));
    }
	public WebElement getElementByXpath(final String xpath) {
    	return getElement(byXpath(xpath));
    }
	public List<WebElement> getElementsByXpath(final String xpath) {
    	return service.getElements(byXpath(xpath));
    }
	public WebElement getElementByCss(String selector) {
    	return getElement(byCss(selector));
    }
	public List<WebElement> getElements(String selector) {
    	return getElements(byCss(selector));
    }
    
	public WebElement getElement(By by) {
		return service.getElement(by);
    }
	public List<WebElement> getElements(By by) {
		return service.getElements(by);
    }
    
	public WebElement waitForElementById(final String id) {
    	return waitForElementById(id, wait) ;
    }
	public WebElement waitForElementById(final String id, final int seconds) {
    	return waitForElement(byId(id), seconds) ;
    }
	public WebElement waitForElementByName(final String name) {
    	return waitForElementByName(name, wait) ;
    }
	public WebElement waitForElementByName(final String name, final int seconds) {
    	return waitForElement(byName(name), seconds) ;
    }
	public WebElement waitForElementByXpath(final String xpath) {
    	return waitForElementByXpath(xpath, wait) ;
    }
	public WebElement waitForElementByXpath(final String xpath, final int seconds) {
    	return waitForElement(byXpath(xpath), seconds) ;
    }
	public WebElement waitForElementByCss(final String selector) {
    	return waitForElementByCss(selector, wait) ;
    }
	public WebElement waitForElementByCss(final String selector, final int seconds) {
    	return waitForElement(byCss(selector), seconds) ;
    }
    
	public WebElement waitForElement(final By by) {
    	return waitForElement(by, wait);
    }
	public WebElement waitForElement(final By by, final int seconds) {
    	return waitFor(seconds, visibilityOf(by));
    }

	public void withNewWindow( boolean close, Closure closure ) {
		withNewWindow(wait, close, closure);
	}

	public void withNewWindow( int seconds, boolean close, Closure closure ) {
		String mainWindowHandle = service.getWindowHandle();
		String handle = waitForNewWindow( seconds );
		if( handle != null ) {
			LOGGER.debug( "withNewWindow: new window - handle: " + handle );
			switchToWindow( handle );
			try {
				closure.call();
			}
			finally {
				if( close ) {
					try {
						service.closeWindow();
					}
					catch (Exception ex) {
						LOGGER.error("Error closing popup window: " + handle);
					}
				}
				LOGGER.debug( "withNewWindow: returning to main window - handle: " + mainWindowHandle );
				switchToWindow(mainWindowHandle);
			}
		}
		else {
			throw new RuntimeException( "Timed out waiting for new window");
		}
	}

	public void waitForNewWindow() {
		waitForNewWindow(wait);
	}

	public String waitForNewWindow(int seconds) {
		return service.waitForNewWindow( seconds );
	}

	public void switchToFrameByIndex( int index ) {
		service.switchToFrameByIndex(index);
	}
	public void switchToFrameByNameOrId( String nameOrId ) {
		service.switchToFrameByNameOrId(nameOrId);
	}
	public void switchToFrameByElement( WebElement element ) {
		service.switchToFrameByElement(element);
	}
	public void switchToWindow( String nameOrHandle ) {
		service.switchToWindow(nameOrHandle);
	}
	public void switchToDefaultContent() {
		service.switchToDefaultContent();
	}
	public WebElement switchToActiveElement() {
		return service.switchToActiveElement();
	}

	public void back() {
		service.back();
	}
	public void forward() {
		service.forward();
	}
	public void to(String url) {
		try {
			to(new URL(url));
		}
		catch( MalformedURLException ex ) {
			throw new RuntimeException( "Invalid url: '" + url + "'", ex );
		}
	}
	public void to(URL url) {
		service.to(url);
	}
	public void refresh() {
		service.refresh();
	}

	public Select select( WebElement element ) {
		return new Select( element );
	}

	public <X> X waitFor(ExpectedCondition<X> cond) {
    	return waitFor(wait, cond);
    }
    
	public <X> X waitFor(final int seconds, final ExpectedCondition<X> cond) {
		return service.waitFor(seconds, cond);
    }
    
	public WebElement clickElementById(final String id) {
    	return clickElementById(id, wait) ;
    }
	public WebElement clickElementById(final String id, final int seconds) {
    	return clickElement(byId(id), seconds) ;
    }
	public WebElement clickElementByName(final String name) {
    	return clickElementByName(name, wait) ;
    }
	public WebElement clickElementByName(final String name, final int seconds) {
    	return clickElement(byName(name), seconds) ;
    }
    
	public WebElement clickElementByXpath(final String xpath) {
    	return clickElementByXpath(xpath, wait) ;
    }
	public WebElement clickElementByXpath(final String xpath, final int seconds) {
    	return clickElement(byXpath(xpath), seconds) ;
    }
    
	public WebElement clickElementByCss(final String selector) {
    	return clickElementByCss(selector, wait) ;
    }
	public WebElement clickElementByCss(final String selector, final int seconds) {
    	return clickElement(byCss(selector), seconds) ;
    }
    
	public WebElement clickElement(final By by, final int seconds) {
		return service.clickElement(by, seconds);
    }

	public WebElement moveToElementById(final String id) {
		return moveToElementById(id, wait) ;
	}
	public WebElement moveToElementById(final String id, final int seconds) {
		return moveToElement(byId(id), seconds) ;
	}
	public WebElement moveToElementByName(final String name) {
		return moveToElementByName(name, wait) ;
	}
	public WebElement moveToElementByName(final String name, final int seconds) {
		return moveToElement(byName(name), seconds) ;
	}
	public WebElement moveToElementByXpath(final String xpath) {
		return moveToElementByXpath(xpath, wait) ;
	}
	public WebElement moveToElementByXpath(final String xpath, final int seconds) {
		return moveToElement(byXpath(xpath), seconds) ;
	}
	public WebElement moveToElementByCss(final String selector) {
		return moveToElementByCss(selector, wait) ;
	}
	public WebElement moveToElementByCss(final String selector, final int seconds) {
		return moveToElement(byCss(selector), seconds) ;
	}

	public WebElement moveToElement(final By by, final int seconds) {
		return service.moveToBy(by, seconds);
	}

	public WebElement moveToElement(WebElement element) {
		return moveToElement(element, wait);
	}

	public WebElement moveToElement(final WebElement element, final int seconds) {
		return service.moveToElement(element, seconds);
	}

	public void mouseDown(WebElement element) {
		service.mouseDown( element );
    }
	public void mouseUp(WebElement element) {
		service.mouseUp( element );
    }
   
	public void screenshot(File directory, String fullClassName, String methodName, String driverName, String ext)
        throws IOException
    {
        String clsName = fullClassName;
        int pos = clsName.lastIndexOf(".");
        if( pos != -1 ) {
            clsName = clsName.substring(pos+1);
        }
        String fileName = clsName + "-" + methodName + "-" + driverName; 
        if( ext != null ) {
            fileName += "-" + ext;
	    }
        fileName += ".png";
        
        screenshot(new File(directory, fileName));
    }
    
	public void screenshot(File file) throws IOException {
		service.screenshot(file);
    }

	public ExpectedCondition<WebElement> presenceOf(By by) {
		return new RemotableConditions.PresenceOf( by );
    }
	public ExpectedCondition<List<WebElement>> presenceOfAll(By by) {
		return new RemotableConditions.PresenceOfAll( by );
    }
	public ExpectedCondition<WebElement> visibilityOf(By by) {
		return new RemotableConditions.VisibilityOf( by );
    }
	public ExpectedCondition<List<WebElement>> visibilityOfAll(By by) {
		return new RemotableConditions.VisibilityOfAll( by );
    }
	public ExpectedCondition<WebElement> visibilityOf(WebElement element) {
		return new RemotableConditions.VisibilityOfElement( element );
    }
	public ExpectedCondition<Boolean> invisibilityOf(By by) {
		return new RemotableConditions.InvisibilityOf( by );
    }
	public ExpectedCondition<Boolean> invisibilityOf(By by, String text) {
		return new RemotableConditions.InvisibilityOfWithText( by, text );
	}
	public ExpectedCondition<Boolean> textPresent(By by, String text) {
		return new RemotableConditions.TextPresent( by, text );
    }
	public ExpectedCondition<Boolean> textPresent(WebElement element, String text) {
		return new RemotableConditions.TextPresentInElement( element, text );
    }
	public ExpectedCondition<Boolean> valuePresent(By by, String text) {
		return new RemotableConditions.ValuePresent( by, text );
    }
	public ExpectedCondition<Boolean> valuePresent(WebElement element, String text) {
		return new RemotableConditions.ValuePresentInElement( element, text );
    }
    
	public ExpectedCondition<Boolean> selected(By by) {
    	return selected( by, true );
    }
	public ExpectedCondition<Boolean> selected(By by, boolean sel) {
		return new RemotableConditions.Selected( by, sel );
    }
    
	public ExpectedCondition<Boolean> selected(WebElement element) {
		return new RemotableConditions.SelectedElement( element );
    }
    
	public ExpectedCondition<WebElement> clickable(By by) {
		return new RemotableConditions.Clickable( by );
    }
	public ExpectedCondition<WebElement> clickable(WebElement element) {
		return new RemotableConditions.ClickableElement( element );
    }
    
	public ExpectedCondition<List<WebElement>> clickableAll(final By by) {
		return new RemotableConditions.ClickableAll( by );
    }
    
	public ExpectedCondition<WebElement> clickableAny(final By by) {
		return new RemotableConditions.ClickableAny( by );
    }

	public By byId(String val) {
		return By.id(val);
	}
	public By byName(String val) {
		return By.name(val);
	}
	public By byIdOrName(String val) {
		return new ByIdOrName(val);
	}
	public By byClassName(String val) {
		return By.className(val);
	}
	public By byCss(String val) {
		return By.cssSelector(val);
	}
	public By byLinkText(String val) {
		return By.linkText(val);
	}
	public By byPartialLinkText(String val) {
		return By.partialLinkText(val);
	}
	public By byTagName(String val) {
		return By.tagName(val);
	}
	public By byXpath(String val) {
		return By.xpath(val);
	}
	public By byAll(By... val) {
		return new ByAll(val);
	}
	public By byChained(By... val) {
		return new ByChained(val);
	}
	public By byAny(By... val) {
		return new ByAny(val);
	}

	public class ByAny extends By implements Serializable {

    	  private static final long serialVersionUID = 1L;

    	  private By[] bys;

    	  public ByAny(By... bys) {
    	    this.bys = bys;
    	  }

    	  @Override
    	  public WebElement findElement(SearchContext context) {
    	    List<WebElement> elements = findElements(context);
    	    if (elements.isEmpty())
    	      throw new NoSuchElementException("Cannot locate an element using " + toString());
    	    return elements.get(0);
    	  }

    	  @Override
    	  public List<WebElement> findElements(SearchContext context) {
    	    for (By by : bys) {
    	      List<WebElement> elems = by.findElements(context);
    	      if( elems != null && elems.size() > 0 ) {
    	    	  return elems;
    	      }
    	    }
    	    return new ArrayList<WebElement>();
    	  }

    	  @Override
    	  public String toString() {
    	    StringBuilder stringBuilder = new StringBuilder("By.any(");
    	    stringBuilder.append("{");

    	    boolean first = true;
    	    for (By by : bys) {
    	      stringBuilder.append((first ? "" : ",")).append(by);
    	      first = false;
    	    }
    	    stringBuilder.append("})");
    	    return stringBuilder.toString();
    	  }
    }
}

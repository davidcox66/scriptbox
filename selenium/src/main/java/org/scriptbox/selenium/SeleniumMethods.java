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
import org.scriptbox.selenium.remoting.RemotableConditions;
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

    private static final String[] CSS_DELIM = {
            "#",
            ".",
            ">",
            "[",
            " "
    };

    private static final String[] HTML_TAGS = {
            "div",
            "span",
            "h1",
            "h2",
            "h3",
            "body",
            "form",
            "input",
            "button",
            "img",
            "select",
            "table",
            "tbody",
            "tr",
            "td",
            "ul",
            "li",
            "a",
            "p"
    };

	public SeleniumMethods()  {
	}

    public SeleniumMethods(SeleniumService service)  {
        this.service = service;
    }

	public SeleniumService getService() {
		return service;
	}

	public void setService(SeleniumService service) {
		this.service = service;
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

	public String getCurrentUrl() {
		return service.getCurrentUrl();
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

	public void setTimeout( int load, int wait, int script ) {
		Timeout tm = new Timeout();
		tm.setWait( wait );
		tm.setLoad( load );
		tm.setScript( script );
		service.setTimeout( tm );
	}
    public void pause( int seconds ) {
		sleep( seconds * 1000 );
    }
    
	public void load(String url) {
    	service.load(url);
    }
    
	public void execute(String script, Object... args) {
		service.execute(script, args);
    }

	public void executeAsync(String script, Object... args) {
		service.execute(script, args);
    }

    public boolean exists( String val ) {
        return exists( byGuess(val) );
    }
	public boolean exists(By by) {
		return service.isElementExists(by);
    }
    
	public WebElement get(final String id) {
    	return get(byGuess(id));
    }

	public WebElement get(By by) {
		return service.getElement(by);
    }

    public List<WebElement> getAll( String val ) {
        return getAll( byGuess(val) );
    }
    public List<WebElement> getAll( By by ) {
        return service.getElements( by );
    }

    public WebElement locate( String val ) {
        return locate( byGuess(val) );
    }

    public WebElement locate( String val, int seconds ) {
        return locate( byGuess(val), seconds );
    }
	public WebElement locate(final By by) {
    	return locate(by, wait);
    }
	public WebElement locate(final By by, final int seconds) {
    	return waitFor(seconds, visibilityOf(by));
    }

	public void withNewWindow( Windows windows, boolean close, Closure closure ) {
		withNewWindow(windows, close, wait, closure);
	}

	public void withNewWindow( Windows windows, boolean close, int seconds, Closure closure ) {
		String handle = waitForNewWindow( windows, seconds );
		if( handle != null ) {
			LOGGER.debug( "withNewWindow: new window - handle: " + handle );
			switchToWindow( handle );
			try {
				closure.call();
			}
			finally {
				if( close ) {
					try {
						service.closeWindow( handle );
					}
					catch (Exception ex) {
						LOGGER.error("Error closing popup window: " + handle);
					}
				}
				LOGGER.debug( "withNewWindow: returning to main window - handle: " + windows.getCurrent() );
				switchToWindow(windows.getCurrent());
			}
		}
		else {
			throw new RuntimeException( "Timed out waiting for new window");
		}
	}

	public void waitForNewWindow( Windows windows ) {
		waitForNewWindow( windows, wait );
	}

	public String waitForNewWindow( Windows windows, int seconds) {
		return service.waitForNewWindow( windows, seconds );
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

	public Windows getWindows() {
		return service.getWindows();
	}
	public String openWindow( String url ) {
		return service.openWindow(url);
	}
	public void closeWindow( String handle ) {
		service.closeWindow( handle );
	}
	public void closeCurrentWindow() {
		service.closeCurrentWindow();
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

    public String alertGetText() {
        return service.alertGetText();
    }
    public void alertDismiss() {
        service.alertDismiss();
    }
    public void alertAccept() {
        service.alertAccept();
    }
    public void alertSendKeys( String keysToSend ) {
        service.alertSendKeys( keysToSend );
    }

    public <X> X waitFor(ExpectedCondition<X> cond) {
    	return waitFor(wait, cond);
    }
    
	public <X> X waitFor(final int seconds, final ExpectedCondition<X> cond) {
		return service.waitFor(seconds, cond);
    }
    
    public WebElement click( String val ) {
        return click( val, wait );
    }

    public WebElement click( String val, int seconds ) {
        return click( byGuess(val), seconds );
    }

	public WebElement click(final By by, final int seconds) {
		return service.clickElement(by, seconds);
    }

    public WebElement move(String val ) {
        return move( byGuess(val), wait );
    }
    public WebElement move(String val, final int seconds) {
        return move( byGuess(val), seconds ) ;
    }
	public WebElement move(final By by, final int seconds) {
		return service.moveToBy(by, seconds);
	}

	public WebElement move(WebElement element) {
		return move(element, wait);
	}

	public WebElement move(final WebElement element, final int seconds) {
		return service.moveToElement(element, seconds);
	}

	public void mouseDown(WebElement element) {
		service.mouseDown( element );
    }
	public void mouseUp(WebElement element) {
		service.mouseUp( element );
    }
	public void mouseMove(WebElement element) {
		service.mouseMove(element);
	}
	public void mouseClick(WebElement element) {
		service.mouseClick(element);
	}
	public void mouseDoubleClick(WebElement element) {
		service.mouseDoubleClick(element);
	}
	public void mouseContextClick(WebElement element) {
		service.mouseContextClick(element);
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
	public ExpectedCondition<Boolean> invisibilityOf(WebElement element) {
		return new RemotableConditions.InvisibilityOfElement( element );
	}
	public ExpectedCondition<Boolean> invisibilityOfAll(By by) {
		return new RemotableConditions.InvisibilityOfAll( by );
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

    public By byGuess( String val ) {
        if( isXpath(val) ) {
           return byXpath( val );
        }
        else if( isCss(val) ) {
            return byCss( val );
        }
        else {
            return byId( val );
        }
    }

    public boolean isXpath( String val ) {
        return val.startsWith( "/" );
    }

    public boolean isCss( String val ) {
        if( isCssDelim(val.substring(0,1)) ) {
            return true;
        }
        else {
            String tag = getHtmlTag( val );
            if( tag != null ) {
                int len = tag.length();
                if( val.length() > len ) {
                    String next = val.substring(len, len+1);
                    return isCssDelim(next);
                }
                return true;

            }
            return false;
        }
    }

    public boolean isCssDelim( String val ) {
        for( String sym : CSS_DELIM ) {
            if( val.equals(sym) ) {
                return true;
            }
        }
        return false;
    }

    public String getHtmlTag( String val ) {
        for( String tag : HTML_TAGS ) {
            if( val.startsWith(tag) ) {
                return tag;
            }
        }
        return null;
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

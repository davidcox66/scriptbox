package org.scriptbox.selenium.driver;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.scriptbox.selenium.Windows;
import org.scriptbox.selenium.remoting.RemotableConditions;
import org.scriptbox.selenium.SeleniumService;
import org.scriptbox.selenium.remoting.SeleniumServiceOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class DriverSeleniumService implements SeleniumService {

    private static final Logger LOGGER = LoggerFactory.getLogger( DriverSeleniumService.class );
    private static final int RETRY_SECONDS = 1;

	private SeleniumController controller;

	public interface WebElementAction
	{
		WebElement run(int seconds);
	}

	public DriverSeleniumService()  {
	}

	public DriverSeleniumService( SeleniumController controller )  {
		this.controller = controller;
	}

	public SeleniumController getController() {
		// support initialization from cli
		return controller != null ? controller : SeleniumController.getInstance();
	}

	public WebDriver getDriver() {
		return getController().getDriver();
	}

    public RemoteWebDriver getRemoteWebDriver() {
        return (RemoteWebDriver)getDriver();
    }
   
	@Override
	public boolean activate( ExpectedCondition<Boolean> cond ) {
		return getController().activate(cond);
	}

    @Override
	public void connect() {
    	getController().connect();
    }

    @Override
	public void disconnect() {
    	getController().disconnect();
    }
    @Override
	public void quit() {
    	getController().quit();
    }

	@Override
	public void get(String url) {
    	LOGGER.debug( "get: url='" + url + "'" );
    	getDriver().get(url);
    }
    
    @Override
	public Object execute(String script, Object... args) {
    	try {
    		LOGGER.debug( "execute: script='" + script + "', args=" + args );
	    	return ((JavascriptExecutor) getDriver()).executeScript(script, args);
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed execute('" + script + "'," + args + ")", ex );
    	}
    }
    @Override
	public void executeAsync(String script, Object... args) {
    	try {
	    	LOGGER.debug( "executeAsync: script='" + script + "', args=" + args );
	    	((JavascriptExecutor) getDriver()).executeAsyncScript(script, args);
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed executeAsync('" + script + "'," + args + ")", ex );
    	}
    }

    @Override
	public boolean isElementExists(By by) {
    	try {
	    	LOGGER.debug( "isElementExistsBy: by='" + by + "'" );
	        List<WebElement> elements = getDriver().findElements( by );
	        return elements != null && elements.size() > 0; 
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed isElementExistsBy(" + by + ")", ex );
    	}
    }
    
    @Override
	public WebElement getElement(By by) {
    	LOGGER.debug("getElement: by='" + by + "'");
        return getDriver().findElement(by);
    }
    @Override
	public List<WebElement> getElements(By by) {
    	LOGGER.debug("getElements: by='" + by + "'");
        return getDriver().findElements(by);
    }
    
    @Override
	public WebElement waitForElement(final By by, final int seconds) {
    	LOGGER.debug("waitForElement: by='" + by + "', seconds=" + seconds);
    	return waitFor(seconds, ExpectedConditions.visibilityOfElementLocated(by));
    }

	@Override
	public String waitForNewWindow( final Windows windows, int seconds) {
		WebDriver d = getDriver();
		LOGGER.debug( "waitForNewWindow: existing=" + windows.getAll() );
		waitFor(seconds, new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return (d.getWindowHandles().size() > windows.getAll().size());
			}
		});
		Set<String> handles = d.getWindowHandles();
		LOGGER.debug("waitForNewWindow: handles=" + handles);

		handles.removeAll( windows.getAll() );
		if( handles.size() > 0 ) {
			String handle = handles.iterator().next();
            LOGGER.debug( "waitForNewWindow: found new window - handle=" + handle );
            return handle;
		}
		LOGGER.debug("waitForNewWindow: did not find new window");
		return null;
	}

    @Override
	public <X> X waitFor(final int seconds, final ExpectedCondition<X> cond) {
    	LOGGER.debug( "waitFor: cond='" + cond + "', seconds=" + seconds );
        return (new WebDriverWait(getDriver(), seconds)).until(cond);
    }
    
    @Override
	public WebElement clickElement(final By by, final int seconds) {
		return actionWithRetry(seconds, new WebElementAction() {
			public WebElement run(int seconds) {
				WebElement element = waitFor(seconds, new RemotableConditions.ClickableAny(by));
				LOGGER.debug("clickElement: clicking  by: " + by);
				element.click();
				return element;
			}
		});
    }

	@Override
	public WebElement moveToBy(final By by, final int seconds) {
		return actionWithRetry(seconds, new WebElementAction() {
			public WebElement run(int seconds) {
				WebElement element = waitFor(seconds, new RemotableConditions.ClickableAny(by));
				LOGGER.debug("moveToElement: moving  by: " + by);
				actions().moveToElement(element);
				return element;
			}
		});
	}

	@Override
	public WebElement moveToElement(final WebElement element, final int seconds) {
		return actionWithRetry(seconds, new WebElementAction() {
			public WebElement run(int seconds) {
				LOGGER.debug("moveToElement: moving  to: " + element);
				actions().moveToElement(element);
				return element;
			}
		});
	}

	@Override
	public void mouseDown(WebElement element) {
    	LOGGER.debug( "mouseDown: element=" + element );
		getMouse().mouseDown(getCoordinates(element));
    }
    @Override
	public void mouseUp(WebElement element) {
    	LOGGER.debug( "mouseUp: element=" + element );
		getMouse().mouseUp(getCoordinates(element));
    }

	@Override
	public void mouseMove(WebElement element) {
		LOGGER.debug( "mouseMove: element=" + element );
		getMouse().mouseMove(getCoordinates(element));
	}

	@Override
	public void mouseClick(WebElement element) {
		LOGGER.debug( "mouseClick: element=" + element );
		getMouse().click(getCoordinates(element));
	}

	@Override
	public void mouseDoubleClick(WebElement element) {
		LOGGER.debug( "mouseDoubleClick: element=" + element );
		getMouse().doubleClick( getCoordinates(element) );
	}

	@Override
	public void mouseContextClick(WebElement element) {
		LOGGER.debug( "mouseContextClick: element=" + element );
		getMouse().contextClick(getCoordinates(element));
	}

	@Override
	public void screenshot(File file) throws IOException {
        LOGGER.info("screenshot: file=" + file);
        File srcFile = ((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.FILE);    
        File parent = file.getParentFile();
        if( !parent.exists() ) {
            parent.mkdirs();
        }
        FileUtils.copyFile(srcFile, file);
    }


	@Override
	public String getCurrentUrl() {
		return getDriver().getCurrentUrl();
	}

	@Override
	public Windows getWindows() {

		Windows ret = new Windows();
		WebDriver d = getDriver();
        ret.setCurrent(d.getWindowHandle());
		ret.setAll( d.getWindowHandles() );
		return ret;
	}

	@Override
	public void closeWindow(String handle) {
		Windows win = getWindows();
		LOGGER.debug( "closeWindow: handle=" + handle + ", windows=" + win );
		if( !win.isCurrentWindow(handle) ) {
			switchToWindow( handle );
			getDriver().close();
			switchToAnyOtherWindow( win );
		}
		else {
			closeCurrentWindow();
		}
	}

	@Override
	public void closeCurrentWindow() {
		Windows windows = getWindows();
		getDriver().close();
        switchToAnyOtherWindow( windows );
	}

	@Override
	public String openWindow( String url ) {
		activate( null );

		Windows windows = getWindows();
		String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
		Object element = execute(String.format(script, url));
		if (element instanceof WebElement) {
			WebElement anchor = (WebElement) element; anchor.click();
			execute("var a=arguments[0];a.parentNode.removeChild(a);", anchor);
			String handle = waitForNewWindow( windows, getController().getTimeout() );
			LOGGER.debug( "openWindow: new window handle=" + handle );
			return handle;
		}
		else {
			throw new RuntimeException("Unable to open window: '" + url + "'" );
		}
	}

	public void switchToFrameByIndex( int index ) {
		switchTo().frame(index);
	}
	public void switchToFrameByNameOrId( String nameOrId ) {
		switchTo().frame(nameOrId);

	}
	public void switchToFrameByElement( WebElement element ) {
		switchTo().frame( element );

	}
	public void switchToWindow( String nameOrHandle ) {
		LOGGER.debug( "switchToWindow: window=" + nameOrHandle );
		switchTo().window(nameOrHandle);

	}
	public void switchToDefaultContent() {
		switchTo().defaultContent();

	}
	public WebElement switchToActiveElement() {
		return switchTo().activeElement();
	}

	public void back() {
		navigate().back();
	}
	public void forward() {
		navigate().forward();
	}
	public void to(URL url) {
		navigate().to(url);
	}
	public void refresh() {
		navigate().refresh();
	}

	@Override
	public void click(WebElement element) {
		LOGGER.debug( "click: element=" + element );
		element.click();
	}

	@Override
	public void submit(WebElement element) {
		LOGGER.debug( "submit: element=" + element );
		element.submit();
	}

	@Override
	public void sendKeys(WebElement element, CharSequence... keysToSend) {
		if( LOGGER.isDebugEnabled() ) {
			StringBuilder builder = new StringBuilder();
			for( CharSequence ks : keysToSend ) {
				if( builder.length() > 0 ) {
					builder.append( "," );
				}
				builder.append( "'" + ks + "'" );
			}
			LOGGER.debug("sendKeys: element=" + element + ", keysToSend=" + builder );
		}
		element.sendKeys( keysToSend );
	}

	@Override
	public void clear(WebElement element) {
		LOGGER.debug( "clear: element=" + element );
		element.clear();
	}

	@Override
	public String getTagName(WebElement element) {
		return element.getTagName();
	}

	@Override
	public String getAttribute(WebElement element, String name) {
		return element.getAttribute( name );
	}

	@Override
	public String getText(WebElement element) {
		return element.getText();
	}

	@Override
	public List<WebElement> findElements(WebElement element, By by) {
		return  element.findElements(by);
	}

	@Override
	public WebElement findElement(WebElement element, By by) {
		return element.findElement(by);
	}

	@Override
	public String getCssValue(WebElement element, String propertyName) {
		return element.getCssValue( propertyName );
	}

	@Override
	public boolean isDisplayed(WebElement element) {
		return element.isDisplayed();
	}

	@Override
	public boolean isSelected(WebElement element) {
		return element.isSelected();
	}

	@Override
	public boolean isEnabled(WebElement element) {
		return element.isEnabled();
	}

	@Override
	public Point getLocation(WebElement element) {
		return element.getLocation();
	}

	@Override
	public Dimension getSize(WebElement element) {
		return element.getSize();
	}

	@Override
	public String download(WebElement element, String attribute, String path, boolean redirects, boolean cookies) {
		try {
			Downloader dl = new Downloader(getDriver());
			dl.setFollowRedirects(redirects);
			dl.setUseCookies(cookies);
			return dl.downloader(element, attribute);
		}
		catch( Exception ex ) {
			throw new RuntimeException( "Failed downloading: element=" + element + ", path=" + path );
		}
	}

	@Override
	public SeleniumServiceOptions getOptions() {
		SeleniumServiceOptions ret = new SeleniumServiceOptions();
		ret.setDownloadDirectory( getController().getOptions().getDownloadDirectory() );
		return ret;
	}

	private Coordinates getCoordinates( WebElement element ) {
		Coordinates ret = ((Locatable) element).getCoordinates();
		LOGGER.debug( "getCoordinates: element=" + element + ", coordinates=" + ret );
		return ret;
	}

	private Mouse getMouse() {
		return getRemoteWebDriver().getMouse();
	}


	private String switchToAnyOtherWindow( Windows win ) {
		for( String handle : win.getNonCurrentWindows() ) {
			try {
				switchToWindow( handle );
				return handle;
			}
			catch( NoSuchWindowException ex ) {
				LOGGER.debug( "switchToAnyOtherWindow: window does not exist: " + handle );
			}
		}
		return null;
	}

	private Actions actions() {
		return new Actions(getDriver());
	}

	private WebDriver.Navigation navigate() {
		return getDriver().navigate();
	}

	private WebDriver.TargetLocator switchTo() {
		return getDriver().switchTo();
	}

	private boolean isRetryableException( WebDriverException ex ) {
		return (ex.getMessage() != null && ex.getMessage().indexOf( "Element is not clickable") >= 0) ||
				ex instanceof StaleElementReferenceException;
	}

	private WebElement actionWithRetry( final int seconds, WebElementAction action ) {
		Exception last = null;
		long timeout = System.currentTimeMillis() + (seconds * 1000);
		while( System.currentTimeMillis() <= timeout ) {
			try {
				int sec = (int)(timeout - System.currentTimeMillis())  / 1000;
				if( sec > 0 ) {
					return action.run(sec);
				}
			}
			catch( WebDriverException ex ) {
				if( !isRetryableException(ex) ) {
					throw ex;
				}
				last = ex;
				LOGGER.debug( "actionWithRetry: retrying...", ex );
				try {
					Thread.sleep( RETRY_SECONDS * 1000 );
				}
				catch( InterruptedException ex2 ) {
					LOGGER.debug( "actionWithRetry: interrupted", ex2 );
				}
			}
			catch( Exception ex ) {
				LOGGER.error( "actionWithRetry: unexpected exception", ex );
			}
		}
		throw new RuntimeException( "Unable to complete action within " + seconds + " seconds" );
	}
}

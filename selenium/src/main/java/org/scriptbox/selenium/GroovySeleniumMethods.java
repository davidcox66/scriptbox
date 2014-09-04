package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ByIdOrName;
import org.openqa.selenium.support.pagefactory.ByAll;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovySeleniumMethods {

    private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumMethods.class );
    
    private static final int DEFAULT_WAIT = 30;
    private static final int RETRY_SECONDS = 1;
    
    private int wait = DEFAULT_WAIT;
    private SeleniumController controller;
    private Binding binding;
    
    public GroovySeleniumMethods( SeleniumController controller )  {
        this.controller = controller;
        bind();
    }

	public void run( String scriptText, String includeText, List<String> parameters ) {
		String text = StringUtils.isNotEmpty(includeText) ? includeText + "\n" + scriptText : scriptText;
		GroovyShell engine = new GroovyShell( binding );
		engine.run(text, "SeleniumScript", parameters );
	}

    public int getWait() {
		return wait;
	}

	public void setWait(int wait) {
		this.wait = wait;
	}

	public WebDriver getDriver() {
		return controller.getDriver();
	}

    public RemoteWebDriver getRemoteWebDriver() {
        return (RemoteWebDriver)getDriver();
    }
   
    private void bind() {
		binding = new Binding();
    	
    	binding.setVariable("log", LOGGER);
    	binding.setVariable("logger", LOGGER);
    	binding.setVariable("LOGGER", LOGGER);
    	
		//binding.setVariable("driver", driver);
		//binding.setVariable("selenium", selenium);
		
		bind( "get" );
		bind( "screenshot" );
		bind( "execute" );
		bind( "executeAsync" );
		
		bind( "isElementExists" );
		bind( "isElementExistsById" );
		bind( "isElementExistsByName" );
		bind( "isElementExistsByXpath" );
		bind( "isElementExistsByCssSelector" );
		
		bind( "getElement" );
		bind( "getElements" );
		bind( "getElementById" );
		bind( "getElementByName" );
		bind( "getElementByXpath" );
		bind( "getElementsByXpath" );
		bind( "getElementByCssSelector" );
		bind( "getElementsByCssSelector" );
		
		bind( "waitFor" );
		bind( "waitForElement" );
		bind( "waitForElementById" );
		bind( "waitForElementByName" );
		bind( "waitForElementByXpath" );
		bind( "waitForElementByCssSelector" );
		
		bind( "clickElement" );
		bind( "clickElementById" );
		bind( "clickElementByName" );
		bind( "clickElementByXpath" );
		bind( "clickElementByCssSelector" );
		
		bind( "mouseDown" );
		bind( "mouseUp" );
		
		bind( "byId" );
		bind( "byName" );
		bind( "byIdOrName" );
		bind( "byCssSelector" );
		bind( "byLinkText" );
		bind( "byPartialLinkText" );
		bind( "byTagName" );
		bind( "byXpath" );
		bind( "byAll" );
		bind( "byAny" );
		bind( "byChained" );
		
		bind( "presenceOf" );
		bind( "presenceOfAll" );
		bind( "visibilityOf" );
		bind( "visibilityOfAll" );
		bind( "invisibilityOf" );
		bind( "textPresent" );
		bind( "valuePresent" );
		bind( "clickable" );
		bind( "clickableAny" );
		bind( "clickableAll" );
		bind( "selected" );
		
		bind( "connect" );
		bind( "disconnect" );
		bind( "quit" );
		
		bind( "sleep" );
		bind( "pause" );
    }
    
    public void connect() {
    	controller.connect();
    }
    public void disconnect() {
    	controller.disconnect();
    }
    public void quit() {
    	controller.quit();
    }
    public void sleep( int millis ) {
    	controller.sleep( millis );
    }
    public void pause( int seconds ) {
    	controller.pause( seconds );
    }
    
    private void bind( String name ) {
    	binding.setVariable( name, new MethodClosure(this, name) );
    }
    
    private void bind( String alias, String name ) {
    	binding.setVariable( alias, new MethodClosure(this, name) );
    }
    
	public void get( String url ) {
    	LOGGER.debug( "get: url='" + url + "'" );
    	getRemoteWebDriver().get( url );
    }
    
    public void execute( String script, Object... args ) {
    	try {
    		LOGGER.debug( "execute: script='" + script + "', args=" + args );
	    	((JavascriptExecutor) getRemoteWebDriver()).executeScript( script, args );
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed execute('" + script + "'," + args + ")", ex );
    	}
    }
    public void executeAsync( String script, Object... args ) {
    	try {
	    	LOGGER.debug( "executeAsync: script='" + script + "', args=" + args );
	    	((JavascriptExecutor) getRemoteWebDriver()).executeAsyncScript( script, args );
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed executeAsync('" + script + "'," + args + ")", ex );
    	}
    }
    public boolean isElementExistsById( String id ) {
        return isElementExists( By.id(id) ); 
    }
    public boolean isElementExistsByName( String name ) {
        return isElementExists( By.name(name) ); 
    }
    public boolean isElementExistsByXpath( String xpath ) {
        return isElementExists( By.xpath(xpath) ); 
    }
    public boolean isElementExistsByCssSelector( String xpath ) {
        return isElementExists( By.cssSelector(xpath) ); 
    }
    
    public boolean isElementExists( By by ) {
    	try {
	    	LOGGER.debug( "isElementExistsBy: by='" + by + "'" );
	        List<WebElement> elements = getDriver().findElements( by );
	        return elements != null && elements.size() > 0; 
    	}
    	catch( Exception ex ) {
    		throw new RuntimeException( "Failed isElementExistsBy(" + by + ")", ex );
    	}
    }
    
    public WebElement getElementById( final String id ) {
    	return getElement( By.id(id) );
    }
    public WebElement getElementByName( final String name ) {
    	return getElement( By.name(name) );
    }
    public WebElement getElementByXpath( final String xpath ) {
    	return getElement( By.xpath(xpath) );
    }
    public List<WebElement> getElementsByXpath( final String xpath ) {
    	return getElements( By.xpath(xpath) );
    }

    public WebElement getElementByCssSelector( String selector ) {
    	return getElement( By.cssSelector(selector) );
    }
    public List<WebElement> getElementsByCssSelector( String selector ) {
    	return getElements( By.cssSelector(selector) );
    }
    
    public WebElement getElement( By by ) {
    	LOGGER.debug( "getElement: by='" + by + "'" );
        return getDriver().findElement( by );
    }
    public List<WebElement> getElements( By by ) {
    	LOGGER.debug( "getElements: by='" + by + "'" );
        return getDriver().findElements( by );
    }
    
    public WebElement waitForElementById( final String id ) {
    	return waitForElementById( id, wait ) ;
    }
    public WebElement waitForElementById( final String id, final int seconds ) {
    	return waitForElement( By.id(id), seconds ) ;
    }
    public WebElement waitForElementByName( final String name ) {
    	return waitForElementByName( name, wait ) ;
    }
    public WebElement waitForElementByName( final String name, final int seconds ) {
    	return waitForElement( By.name(name), seconds ) ;
    }
    
    public WebElement waitForElementByXpath( final String xpath ) {
    	return waitForElementByXpath( xpath, wait ) ;
    }
    public WebElement waitForElementByXpath( final String xpath, final int seconds ) {
    	return waitForElement( By.xpath(xpath), seconds ) ;
    }
    
    public WebElement waitForElementByCssSelector( final String selector ) {
    	return waitForElementByCssSelector( selector, wait ) ;
    }
    public WebElement waitForElementByCssSelector( final String selector, final int seconds ) {
    	return waitForElement( By.cssSelector(selector), seconds ) ;
    }
    
    public WebElement waitForElement( final By by ) {
    	return waitForElement( by, wait );
    }
    public WebElement waitForElement( final By by, final int seconds ) {
    	LOGGER.debug( "waitForElement: by='" + by + "', seconds=" + seconds );
    	return waitFor( by, visibilityOf(by), seconds );
    }

    public <X> X waitFor( final By by, ExpectedCondition<X> cond ) {
    	return waitFor( by, cond, wait );
    }
    
    public <X> X waitFor( final By by, ExpectedCondition<X> cond, final int seconds ) {
    	LOGGER.debug( "waitForElement: by='" + by + "', seconds=" + seconds );
        return (new WebDriverWait(getDriver(), seconds)).until(cond);
    }
    
    public WebElement clickElementById( final String id ) {
    	return clickElementById( id, wait ) ;
    }
    public WebElement clickElementById( final String id, final int seconds ) {
    	return clickElement( By.id(id), seconds ) ;
    }
    public WebElement clickElementByName( final String name ) {
    	return clickElementByName( name, wait ) ;
    }
    public WebElement clickElementByName( final String name, final int seconds ) {
    	return clickElement( By.name(name), seconds ) ;
    }
    
    public WebElement clickElementByXpath( final String xpath ) {
    	return clickElementByXpath( xpath, wait ) ;
    }
    public WebElement clickElementByXpath( final String xpath, final int seconds ) {
    	return clickElement( By.xpath(xpath), seconds ) ;
    }
    
    public WebElement clickElementByCssSelector( final String selector ) {
    	return clickElementByCssSelector( selector, wait ) ;
    }
    public WebElement clickElementByCssSelector( final String selector, final int seconds ) {
    	return clickElement( By.cssSelector(selector), seconds ) ;
    }
    
    public WebElement clickElement( final By by, final int seconds ) {
    	Exception last = null;
    	long timeout = System.currentTimeMillis() + (seconds * 1000);
    	while( System.currentTimeMillis() <= timeout ) {
	    	try {
	    		WebElement element = waitFor( by, clickableAny(by), seconds );
	    		LOGGER.debug( "clickElement: clicking  by: " + by );
	    		element.click();
	    		return element;
	    	}
	    	catch( WebDriverException ex ) {
	    		if( !isRetryableException(ex) ) {
	    			throw ex;
	    		}
	    		last = ex;
	    		LOGGER.debug( "clickElement: retrying: " + by, ex );
	    		try {
		    		Thread.sleep( RETRY_SECONDS * 1000 );
	    		}
	    		catch( InterruptedException ex2 ) {
	    			LOGGER.debug( "clickElement: interrupted", ex2 );
	    		}
	    	}
    	}
    	throw new RuntimeException( "Element was never visible to click - by: " + by, last );
    }
    
    public void mouseDown( WebElement element ) {
    	LOGGER.debug( "mouseDown: element=" + element );
        Coordinates coord = ((Locatable)element).getCoordinates();
        Mouse mouse = getRemoteWebDriver().getMouse();
        mouse.mouseDown( coord );
    }
    public void mouseUp( WebElement element ) {
    	LOGGER.debug( "mouseUp: element=" + element );
        Coordinates coord = ((Locatable)element).getCoordinates();
        Mouse mouse = getRemoteWebDriver().getMouse();
        mouse.mouseDown( coord );
    }
   
    public void screenshot( File directory, String fullClassName, String methodName, String driverName, String ext ) 
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
        
        screenshot( new File(directory,fileName) );
    }
    
	public void screenshot( File file ) throws IOException {
        LOGGER.info( "screenshot: file=" + file );
        File srcFile = ((TakesScreenshot)getDriver()).getScreenshotAs(OutputType.FILE);    
        File parent = file.getParentFile();
        if( !parent.exists() ) {
            parent.mkdirs();
        }
        FileUtils.copyFile( srcFile, file );
    }

	public boolean isRetryableException( WebDriverException ex ) {
		return (ex.getMessage() != null && ex.getMessage().indexOf( "Element is not clickable") >= 0) ||
				ex instanceof StaleElementReferenceException;
	}
	
    public By byId( String val ) {
    	return By.id(val);
    }
    public By byName( String val ) {
    	return By.name(val);
    }
    public By byIdOrName( String val ) {
    	return new ByIdOrName(val);
    }
    public By byClassName( String val ) {
    	return By.className(val);
    }
    public By byCssSelector( String val ) {
    	return By.cssSelector(val);
    }
    public By byLinkText( String val ) {
    	return By.linkText(val);
    }
    public By byPartialLinkText( String val ) {
    	return By.partialLinkText(val);
    }
    public By byTagName( String val ) {
    	return By.tagName(val);
    }
    public By byXpath( String val ) {
    	return By.xpath(val);
    }
    public By byAll( By... val ) {
    	return new ByAll(val);
    }
    public By byChained( By... val ) {
    	return new ByChained(val);
    }
    public By byAny( By... val ) {
    	return new ByAny(val);
    }
    
    public ExpectedCondition<WebElement> presenceOf( By by ) {
    	return ExpectedConditions.presenceOfElementLocated( by );
    }
    public ExpectedCondition<List<WebElement>> presenceOfAll( By by ) {
    	return ExpectedConditions.presenceOfAllElementsLocatedBy( by );
    }
    public ExpectedCondition<WebElement> visibilityOf( By by ) {
    	return ExpectedConditions.visibilityOfElementLocated( by );
    }
    public ExpectedCondition<List<WebElement>> visibilityOfAll( By by ) {
    	return ExpectedConditions.visibilityOfAllElementsLocatedBy( by );
    }
    public ExpectedCondition<WebElement> visibilityOf( WebElement element ) {
    	return ExpectedConditions.visibilityOf( element );
    }
    public ExpectedCondition<Boolean> invisibilityOf( By by ) {
    	return ExpectedConditions.invisibilityOfElementLocated( by );
    }
    public ExpectedCondition<Boolean> invisibilityOf( By by, String text ) {
    	return ExpectedConditions.invisibilityOfElementWithText( by, text );
    }
    
    public ExpectedCondition<Boolean> textPresent( By by, String text ) {
    	return ExpectedConditions.textToBePresentInElementLocated( by, text );
    }
    public ExpectedCondition<Boolean> textPresent( WebElement element, String text ) {
    	return ExpectedConditions.textToBePresentInElement( element, text );
    }
    public ExpectedCondition<Boolean> valuePresent( By by, String text ) {
    	return ExpectedConditions.textToBePresentInElementValue( by, text );
    }
    public ExpectedCondition<Boolean> valuePresent( WebElement element, String text ) {
    	return ExpectedConditions.textToBePresentInElementValue( element, text );
    }
    
    public ExpectedCondition<Boolean> selected( By by ) {
    	return selected( by, true );
    }
    public ExpectedCondition<Boolean> selected( By by, boolean sel ) {
    	return ExpectedConditions.elementSelectionStateToBe( by, sel );
    }
    
    public ExpectedCondition<Boolean> selected( WebElement element ) {
    	return ExpectedConditions.elementToBeSelected( element );
    }
    
    public ExpectedCondition<WebElement> clickable( By by ) {
    	return ExpectedConditions.elementToBeClickable( by );
    }
    public ExpectedCondition<WebElement> clickable( WebElement element) {
    	return ExpectedConditions.elementToBeClickable( element );
    }
    
    public ExpectedCondition<List<WebElement>> clickableAll( final By by ) {
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
    
    public ExpectedCondition<WebElement> clickableAny( final By by ) {
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
    
    public class ByAny extends By implements Serializable {

    	  private static final long serialVersionUID = 1563769051170172451L;

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

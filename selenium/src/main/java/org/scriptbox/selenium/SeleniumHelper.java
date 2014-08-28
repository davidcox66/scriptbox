package org.scriptbox.selenium;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger( SeleniumHelper.class );
    
    private static final int DEFAULT_WAIT = 30;
   
    private String name;
    private WebDriver driver;
    
    public SeleniumHelper( String name, WebDriver driver )  {
        this.name = name;
        this.driver = driver;
    }
   
    public String getName() {
        return name;
    }

    public void get( String url ) {
    	LOGGER.debug( "get: url='" + url + "'" );
    	getRemoteWebDriver().get( url );;
    }
    
    public void execute( String script, Object... args ) {
    	LOGGER.debug( "execute: script='" + script + "', args=" + args );
    	((JavascriptExecutor) getRemoteWebDriver()).executeScript( script, args );
    }
    public void executeAsync( String script, Object... args ) {
    	LOGGER.debug( "executeAsync: script='" + script + "', args=" + args );
    	((JavascriptExecutor) getRemoteWebDriver()).executeAsyncScript( script, args );
    }
    public boolean isElementExistsById( String id ) {
    	LOGGER.debug( "isElementExistsById: id='" + id + "'" );
        return isElementExists( By.id(id) ); 
    }
    
    public boolean isElementExistsByXpath( String xpath ) {
    	LOGGER.debug( "isElementExistsByXpath: xpath='" + xpath + "'" );
        return isElementExists( By.xpath(xpath) ); 
    }
    
    public boolean isElementExists( By by ) {
    	LOGGER.debug( "isElementExistsBy: by='" + by + "'" );
        List<WebElement> elements = driver.findElements( by );
        return elements != null && elements.size() > 0; 
    }
    
    public WebElement getElementById( final String id ) {
    	LOGGER.debug( "getElementById: id='" + id + "'" );
        return driver.findElement( By.id(id) );
    }
    
    public WebElement getElementByXpath( final String xpath ) {
    	LOGGER.debug( "getElementByXpath: xpath='" + xpath + "'" );
        return driver.findElement( By.xpath(xpath) );
    }
    public List<WebElement> getElementsByXpath( final String xpath ) {
    	LOGGER.debug( "getElementsByXpath: xpath='" + xpath + "'" );
        return driver.findElements( By.xpath(xpath) );
    }

    public WebElement getElementBySelector( String selector ) {
    	LOGGER.debug( "getElementBySelector: selector='" + selector + "'" );
        return driver.findElement( By.cssSelector(selector) );
    }
    public List<WebElement> getElementsBySelector( String selector ) {
    	LOGGER.debug( "getElementsBySelector: selector='" + selector + "'" );
        return driver.findElements( By.cssSelector(selector) );
    }
    
    public WebElement waitForElementById( final String id ) {
    	LOGGER.debug( "waitForElementById: id='" + id + "'" );
    	return waitForElementById( id, DEFAULT_WAIT ) ;
    }
    
    public WebElement waitForElementById( final String id, final int seconds ) {
    	LOGGER.debug( "waitForElementById: id='" + id + "', seconds=" + seconds );
    	return waitForElement( By.id(id), seconds ) ;
    }
    
    public WebElement waitForElementByXpath( final String xpath ) {
    	LOGGER.debug( "waitForElementByXpath: xpath='" + xpath + "'" );
    	return waitForElementByXpath( xpath, DEFAULT_WAIT ) ;
    }
    
    public WebElement waitForElementByXpath( final String xpath, final int seconds ) {
    	LOGGER.debug( "waitForElementByXpath: xpath='" + xpath + "', seconds=" + seconds );
    	return waitForElement( By.xpath(xpath), seconds ) ;
    }
    
    public WebElement waitForElementBySelector( final String selector ) {
    	LOGGER.debug( "waitForElementBySelector: selector='" + selector + "'" );
    	return waitForElementBySelector( selector, DEFAULT_WAIT ) ;
    }
    public WebElement waitForElementBySelector( final String selector, final int seconds ) {
    	LOGGER.debug( "waitForElementBySelector: selector='" + selector + "', seconds=" + seconds );
    	return waitForElement( By.cssSelector(selector), seconds ) ;
    }
    
    public WebElement waitForElement( final By by, final int seconds ) {
    	LOGGER.debug( "waitForElement: by='" + by + "', seconds=" + seconds );
        return (new WebDriverWait(driver, seconds)).until(ExpectedConditions.visibilityOfElementLocated(by));
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
   
    public WebDriver getWebDriver() {
        return driver;
    }
   
    public RemoteWebDriver getRemoteWebDriver() {
        return (RemoteWebDriver)driver;
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
        File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);    
        File parent = file.getParentFile();
        if( !parent.exists() ) {
            parent.mkdirs();
        }
        FileUtils.copyFile( srcFile, file );
    }
}

package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.runtime.MethodClosure;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroovySelenium {

	private static final Logger LOGGER = LoggerFactory.getLogger( GroovySelenium.class );

	private DriverType type;
	private URL url;
	private String profile;
    private boolean quit;
    private int timeout;
    
    private RemoteWebDriver driver;
    private SeleniumMethods selenium;
    private Binding binding;
    
    public enum DriverType {
    	FIREFOX("firefox") {
            public RemoteWebDriver create( URL url, String profile ) {
                if( url != null ) {
    	           DesiredCapabilities cap = DesiredCapabilities.firefox();
    	           if( profile != null ) {
    		           cap.setCapability(FirefoxDriver.PROFILE, profile);
    	           }
    		       RemoteWebDriver driver = new RemoteWebDriver(url, cap);
    		       return (RemoteWebDriver)new Augmenter().augment(driver);
                }
                else {
    		        File profileDir = new File(profile);
    		        return new FirefoxDriver( new FirefoxProfile(profileDir) ); 
                }
            }
    	},
    	CHROME("chrome") {
    		public RemoteWebDriver create( URL url, String profile ) {
               DesiredCapabilities cap = DesiredCapabilities.chrome();
               cap.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors"));
    	       RemoteWebDriver driver = new RemoteWebDriver(url, cap);
               return (RemoteWebDriver)new Augmenter().augment(driver);
            }
    	},
    	IE("ie") {
            public RemoteWebDriver create( URL url, String profile ) {
               DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
               cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);   
    	       RemoteWebDriver driver = new RemoteWebDriver(url, cap);
               return (RemoteWebDriver)new Augmenter().augment(driver);
            }
    	};
    	
    	private String name;
    	
    	private DriverType( String name ) {
    		this.name = name;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public RemoteWebDriver create( URL url, String profile ) {
    		return null;
    	}
    };

    
	public DriverType getType() {
		return type;
	}

	public void setType(DriverType type) {
		this.type = type;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public boolean isQuit() {
		return quit;
	}

	public void setQuit(boolean quit) {
		this.quit = quit;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void run( String scriptText, String includeText, List<String> parameters ) {
		String text = StringUtils.isNotEmpty(includeText) ? includeText + "\n" + scriptText : scriptText;
		GroovyShell engine = new GroovyShell( binding );
		engine.run(text, "SeleniumScript", parameters );
	}

	public void quit() {
        if( driver != null && quit ) {
            try {
	            driver.quit();
            }
            catch( Exception ex ) {
            	throw new RuntimeException( "Failed shutting down", ex );
            }
            finally {
            	driver = null;
            	selenium = null;
            }
        }
	}
	
    public void init() {
		driver = type.create( url, profile );
		driver.manage().timeouts().implicitlyWait(timeout > 0 ? timeout : 30, TimeUnit.SECONDS);
		selenium = new SeleniumMethods( type.getName(), driver );
		binding = new Binding();
    	
    	binding.setVariable("log", LOGGER);
    	binding.setVariable("logger", LOGGER);
    	binding.setVariable("LOGGER", LOGGER);
    	
		binding.setVariable("driver", driver);
		binding.setVariable("selenium", selenium);
		bind( binding, "get" );
		bind( binding, "screenshot" );
		bind( binding, "execute" );
		bind( binding, "executeAsync" );
		
		bind( binding, "isElementExists" );
		bind( binding, "isElementExistsById" );
		bind( binding, "isElementExistsByName" );
		bind( binding, "isElementExistsByXpath" );
		bind( binding, "isElementExistsByCssSelector" );
		
		bind( binding, "getElement" );
		bind( binding, "getElements" );
		bind( binding, "getElementById" );
		bind( binding, "getElementByName" );
		bind( binding, "getElementByXpath" );
		bind( binding, "getElementsByXpath" );
		bind( binding, "getElementByCssSelector" );
		bind( binding, "getElementsByCssSelector" );
		
		bind( binding, "waitFor" );
		bind( binding, "waitForElement" );
		bind( binding, "waitForElementById" );
		bind( binding, "waitForElementByName" );
		bind( binding, "waitForElementByXpath" );
		bind( binding, "waitForElementByCssSelector" );
		
		bind( binding, "clickElement" );
		bind( binding, "clickElementById" );
		bind( binding, "clickElementByName" );
		bind( binding, "clickElementByXpath" );
		bind( binding, "clickElementByCssSelector" );
		
		bind( binding, "mouseDown" );
		bind( binding, "mouseUp" );
		
		bind( binding, "byId" );
		bind( binding, "byName" );
		bind( binding, "byIdOrName" );
		bind( binding, "byCssSelector" );
		bind( binding, "byLinkText" );
		bind( binding, "byPartialLinkText" );
		bind( binding, "byTagName" );
		bind( binding, "byXpath" );
		bind( binding, "byAll" );
		bind( binding, "byAny" );
		bind( binding, "byChained" );
		
		bind( binding, "presenceOf" );
		bind( binding, "presenceOfAll" );
		bind( binding, "visibilityOf" );
		bind( binding, "visibilityOfAll" );
		bind( binding, "invisibilityOf" );
		bind( binding, "textPresent" );
		bind( binding, "valuePresent" );
		bind( binding, "clickable" );
		bind( binding, "clickableAny" );
		bind( binding, "clickableAll" );
		bind( binding, "selected" );
		
		ClosureMethods methods = new ClosureMethods(driver,selenium);
		binding.setVariable( "sleep", new MethodClosure(methods,"sleep") );
		binding.setVariable( "pause", new MethodClosure(methods,"pause") );
    }
    
    private void bind( Binding binding, String name ) {
    	binding.setVariable( name, new MethodClosure(selenium, name) );
    }
    
    private void bind( Binding binding, String alias, String name ) {
    	binding.setVariable( alias, new MethodClosure(selenium, name) );
    }

    public static class ClosureMethods
    {
    	RemoteWebDriver driver;
    	SeleniumMethods selenium;
    	
    	public ClosureMethods( RemoteWebDriver driver, SeleniumMethods selenium ) {
    		this.driver = driver;
    		this.selenium = selenium;
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
    }
    
}

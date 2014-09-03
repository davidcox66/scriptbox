package org.scriptbox.selenium;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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

	private static final int DRIVER_RETRIES = 5;
	
	private DriverType type;
	private URL url;
	private String profile;
    private int timeout;
    
    private RemoteWebDriver driver;
    private SeleniumMethods selenium;
    private Binding binding;
    
    private String exe;
    private Process process;
    private List<Thread> readers;
    
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getExe() {
		return exe;
	}

	public void setExe(String exe) {
		this.exe = exe;
	}

	public void run( String scriptText, String includeText, List<String> parameters ) {
		String text = StringUtils.isNotEmpty(includeText) ? includeText + "\n" + scriptText : scriptText;
		GroovyShell engine = new GroovyShell( binding );
		engine.run(text, "SeleniumScript", parameters );
	}

    public void init() throws Exception {
		selenium = new SeleniumMethods( type.getName() );
		initBindings();
    }

    public void connect() throws Exception {
    	connect( DRIVER_RETRIES, 1 );
    }
    
    public void connect( int retries, int delay ) throws Exception {
		RuntimeException last = null;
		for( int i=0 ; i < retries ; i++ ) {
    		try {
		    	try {
			    	startDriverProcess();
			    	pause( delay );
			    	startDriver();
			    	return;
		    	}
		    	catch( Exception ex ) {
		    		disconnect();
		    	}
		    	
    		}
    		catch( RuntimeException ex ) {
    			LOGGER.error( "startDriver: failed connecting to driver", ex );
    			last = ex;
    			pause( delay );
    		}
    		throw new RuntimeException( "Error connecting to: " + url, last );
		}
    }
    
    public void disconnect() {
		stopDriver();
		stopProcess();
		stopReaders();
    }
    
    public void quit() {
    	stopDriver();
    }
    
    private void initBindings() {
		binding = new Binding();
    	
    	binding.setVariable("log", LOGGER);
    	binding.setVariable("logger", LOGGER);
    	binding.setVariable("LOGGER", LOGGER);
    	
		//binding.setVariable("driver", driver);
		//binding.setVariable("selenium", selenium);
		
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
		
		binding.setVariable( "connect", new MethodClosure(this,"connect") );
		binding.setVariable( "disconnect", new MethodClosure(this,"disconnect") );
		binding.setVariable( "quit", new MethodClosure(this,"quit") );
		
		binding.setVariable( "sleep", new MethodClosure(this,"sleep") );
		binding.setVariable( "pause", new MethodClosure(this,"pause") );
    }
    
    private void startDriver() {
    	if( driver == null ) {
    		LOGGER.debug( "startDriver: starting url: " + url + ", profile: " + profile );
			driver = type.create( url, profile );
			driver.manage().timeouts().implicitlyWait(timeout > 0 ? timeout : 30, TimeUnit.SECONDS);
			selenium.setDriver( driver );
    	}
    }
    
    private void startDriverProcess() throws Exception
    {
    	if( StringUtils.isNotEmpty(exe) && process == null ) {
    		LOGGER.debug( "startDriverProcess: starting exe: " + exe );
    		readers = new ArrayList<Thread>();
	    	process = Runtime.getRuntime().exec( exe );
	    	readers.add( consume(process.getInputStream()) );
	    	readers.add( consume(process.getErrorStream()) );
    	}
    }
    
    private void stopProcess() {
    	try {
    		if( process != null ) {
    			LOGGER.debug( "stopProcess: stopping exe: " + exe );
    			process.destroy();
    		}
    	}
    	catch( Exception ex ) {
    		LOGGER.error( "Error stopping driver process", ex );
    	}
    	process = null;

    }
    
    private void stopReaders() {
    	if( readers != null ) {
	    	for( Thread t : readers ) {
	    		try {
		    		t.join();
	    		}
	    		catch( Exception ex ) {
	    			LOGGER.error( "Failed waiting for driver process", ex );
	    		}
	    	}
    	}
    	readers = null;
    }
    
	private void stopDriver() {
        if( driver != null ) {
            try {
            	LOGGER.debug( "stopDriver: stopping driver url: " + url );
	            driver.quit();
            }
            catch( Exception ex ) {
            	LOGGER.error( "Error stopping driver", ex );
            }
        	driver = null;
        }
	}
	
    private void bind( Binding binding, String name ) {
    	binding.setVariable( name, new MethodClosure(selenium, name) );
    }
    
    private void bind( Binding binding, String alias, String name ) {
    	binding.setVariable( alias, new MethodClosure(selenium, name) );
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
    
    private static Thread consume(InputStream input) {
        Thread thread = new Thread(new LogDumper(input));
        thread.start();
        return thread;
    }

    private static class LogDumper implements Runnable {
        InputStream in;

        public LogDumper(InputStream in) {
            this.in = in;
        }

        public void run() {
            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(isr);
            String next;
            try {
                while ((next = br.readLine()) != null) {
                	LOGGER.debug( "driver: " + next );
                }
            } 
            catch (IOException e) {
                throw new RuntimeException("Exception while reading driver stream", e);
            }
        }
    }
}

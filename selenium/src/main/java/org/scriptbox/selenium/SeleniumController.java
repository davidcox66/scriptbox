package org.scriptbox.selenium;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumController {

	private static final Logger LOGGER = LoggerFactory.getLogger( SeleniumController.class );

	private static final int DRIVER_RETRIES = 5;
	
	private DriverType type;
	private URL url;
	private String profile;
    private int timeout;
    
    private RemoteWebDriver driver;
    
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

    public SeleniumController( DriverType type ) {
    	this.type = type;
    }
    
	public DriverType getType() {
		return type;
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

    public void connect() {
    	connect( DRIVER_RETRIES, 1 );
    }
    
    public void connect( int retries, int delay ) {
    	if( !isConnected() ) {
			Exception last = null;
			for( int i=0 ; i < retries ; i++ ) {
		    	try {
			    	startDriverProcess( delay );
			    	startDriver();
			    	return;
		    	}
		    	catch( Exception ex ) {
	    			LOGGER.error( "startDriver: failed connecting to driver", ex );
	    			last = ex;
		    		disconnect();
	    			pause( delay );
	    		}
			}
    		throw new RuntimeException( "Error connecting to: " + url, last );
    	}
    }
    
    public void disconnect() {
		stopDriver();
		stopProcess();
    }
    
    public void quit() {
    	stopDriver();
    }
    
    public boolean isConnected() {
    	return driver != null;
    }
    
    public WebDriver getDriver() {
    	if( !isConnected() ) {
    		connect();
    	}
    	return driver;
    }
    
    private void startDriver() {
    	if( !isConnected() ) {
    		LOGGER.debug( "startDriver: starting url: " + url + ", profile: " + profile );
			driver = type.create( url, profile );
			driver.manage().timeouts().implicitlyWait(timeout > 0 ? timeout : 30, TimeUnit.SECONDS);
    	}
    }
    
    private void startDriverProcess( int delay ) throws Exception
    {
    	if( StringUtils.isNotEmpty(exe) && process == null ) {
    		LOGGER.debug( "startDriverProcess: starting exe: " + exe );
    		readers = new ArrayList<Thread>();
	    	process = Runtime.getRuntime().exec( exe );
	    	readers.add( consume(process.getInputStream()) );
	    	readers.add( consume(process.getErrorStream()) );
	    	pause( delay );
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

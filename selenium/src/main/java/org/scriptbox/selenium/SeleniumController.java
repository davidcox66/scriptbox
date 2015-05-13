package org.scriptbox.selenium;

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

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SeleniumController {

	private static final Logger LOGGER = LoggerFactory.getLogger( SeleniumController.class );

	private static final int DRIVER_RETRIES = 5;
	
    private int timeout;

	private DriverType type;
	private DriverOptions options = new DriverOptions();
    private RemoteWebDriver driver;
    
    private String exe;
    private Process process;
    private List<Thread> readers;
    
    public enum DriverType {
    	FIREFOX("firefox") {
            public RemoteWebDriver create( DriverOptions options ) {
                if( options.getUrl() != null ) {
    	           DesiredCapabilities cap = DesiredCapabilities.firefox();
    	           if( options.getProfile() != null ) {
    		           cap.setCapability(FirefoxDriver.PROFILE, options.getProfile());
    	           }
    		       RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
    		       return (RemoteWebDriver)new Augmenter().augment(driver);
                }
                else {
    		        File profileDir = new File(options.getProfile());
    		        return new FirefoxDriver( new FirefoxProfile(profileDir) ); 
                }
            }
    	},
    	CHROME("chrome") {
    		public RemoteWebDriver create( DriverOptions options ) {
				DesiredCapabilities cap = DesiredCapabilities.chrome();
				if (options.isIgnoreCertificateErrors()) {
					cap.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors"));
				}
    	       	RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
               	return (RemoteWebDriver)new Augmenter().augment(driver);
            }
    	},
    	IE("ie") {
            public RemoteWebDriver create( DriverOptions options ) {
               DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
               cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, options.isAcceptCertificates());
    	       RemoteWebDriver driver = new RemoteWebDriver(options.getUrl(), cap);
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
    	
    	public RemoteWebDriver create( DriverOptions options ) {
    		return null;
    	}
    };

	public static class DriverOptions
	{
		private URL url;
		private String profile;
		private boolean acceptCertificates;
		private boolean ignoreCertificateErrors;

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

		public boolean isAcceptCertificates() {
			return acceptCertificates;
		}

		public void setAcceptCertificates(boolean acceptCertificates) {
			this.acceptCertificates = acceptCertificates;
		}

		public boolean isIgnoreCertificateErrors() {
			return ignoreCertificateErrors;
		}

		public void setIgnoreCertificateErrors(boolean ignoreCertificateErrors) {
			this.ignoreCertificateErrors = ignoreCertificateErrors;
		}
	}

    public SeleniumController( DriverType type ) {
    	this.type = type;
    }
    
	public DriverType getType() {
		return type;
	}

	public DriverOptions getOptions() {
		return options;
	}

	public URL getUrl() {
		return options.getUrl();
	}

	public void setUrl(URL url) {
		options.setUrl( url );
	}

	public String getProfile() {
		return options.getProfile();
	}

	public void setProfile(String profile) {
		options.setProfile( profile );
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
    		throw new RuntimeException( "Error connecting to: " + getUrl(), last );
    	}
    }
    
    public void disconnect() {
		stopDriver();
		stopProcess();
    }
    
    public void quit() {
    	stopDriver();
    }

	public boolean ping() {
		if( !isAvailable() ) {
			if( isConnected() ) {
				quit();
			}
			connect();
			return false;
		}
		return true;
	}

	public boolean isAvailable() {
		if( isConnected() ) {
			try {
				driver.getCurrentUrl();
				return true;
			}
			catch( Exception ex ) {
				LOGGER.error( "isAvailable: error getting current url", ex );
			}
		}
		return false;
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
    		LOGGER.debug( "startDriver: starting url: " + options.getUrl() + ", profile: " + options.getProfile() );
			driver = type.create( options );
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
            	LOGGER.debug( "stopDriver: stopping driver url: " + getUrl() );
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

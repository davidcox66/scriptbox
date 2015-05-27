package org.scriptbox.selenium;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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
	private int port;
    private Process process;
    private List<Thread> readers;

	private static SeleniumController instance;

	public static SeleniumController getInstance() {
		return instance;
	}

	public static void setInstance(SeleniumController instance) {
		SeleniumController.instance = instance;
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
		options.setUrl(url);
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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
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

	public boolean ping( ExpectedCondition<Boolean> cond ) {
		if (cond != null) {
			try {
				if( isConnected() ) {
					return cond.apply(getDriver());
				}
			}
			catch (Exception ex) {
				LOGGER.error("Error pinging", ex);
			}
			return false;
		}
		return true;
	}

	public boolean activate( ExpectedCondition<Boolean> cond ) {
		if ( !ping(cond) ) {
			if (!isResponsive()) {
				if (isConnected()) {
					quit();
				}
				connect();
				return false;
			}
		}
        return true;
	}

	public String getCurrentUrl() {
		if( isConnected() ) {
			try {
				return driver.getCurrentUrl();
			}
			catch (Exception ex) {
				LOGGER.error("getCurrentUrl: error getting current url", ex);
			}
		}
		return null;
	}

	public boolean isResponsive() {
		if( isConnected() ) {
			try {
				driver.getCurrentUrl();
				return true;
			}
			catch (Exception ex) {
				LOGGER.error("isResponsive: error getting current url", ex);
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
	    	process = Runtime.getRuntime().exec( exe + " --port=" + getPort() );
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
	

    private static Thread consume(InputStream input) {
        Thread thread = new Thread(new LogDumper(input));
        thread.start();
        return thread;
    }

	private void pause( int seconds ) {
		try {
			Thread.sleep(seconds * 1000);
		}
		catch( Exception ex2 ) {
			LOGGER.error( "Error while pausing", ex2 );
		}
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

package org.scriptbox.selenium;

import groovy.lang.Closure;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
	    			Selenium.pause( delay );
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

	public boolean ping( SeleniumPing sp ) {
		if (sp != null) {
			try {
				return sp.ping(this);
			} catch (Exception ex) {
				LOGGER.error("Error pinging", ex);
			}
			return false;
		}
		return true;
	}

	public boolean activate( SeleniumPing sp ) {
		if ( !ping(sp) ) {
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

	public boolean isAtLocation( String baseUrl ) {
		if( isConnected() ) {
			try {
				String current = driver.getCurrentUrl();
				LOGGER.debug("isAtLocation: base: " + baseUrl + ", current: " + current);
				return current != null && current.startsWith(baseUrl);
			}
			catch (Exception ex) {
				LOGGER.error("isAtLocation: error getting current url", ex);
			}
		}
		return false;
	}

	public boolean isAtLocation( Pattern pattern ) {
		if( isConnected() ) {
			try {
				String current = driver.getCurrentUrl();
				LOGGER.debug("isAtLocation: pattern: " + pattern + ", current: " + current);
				return current != null && pattern.matcher(current).matches();
			}
			catch (Exception ex) {
				LOGGER.error("isAtLocation: error getting current url", ex);
			}
		}
		return false;
	}

	public boolean isAtLocation( Closure closure ) {
		if( isConnected() ) {
			try {
				String current = driver.getCurrentUrl();
				LOGGER.debug("isAtLocation: current: " + current);
				if( current != null ) {
					int max = closure.getMaximumNumberOfParameters();
					if( max == 1 ) {
						return (Boolean)closure.call( current );
					}
					else if( max == 2 ) {
						return (Boolean)closure.call( current, this );
					}
					else {
						throw new RuntimeException( "Too many parameters to closure" );
					}
				}
			}
			catch (Exception ex) {
				LOGGER.error("isAtLocation: error getting current url", ex);
			}
		}
		return false;
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
	    	process = Runtime.getRuntime().exec( exe );
	    	readers.add( consume(process.getInputStream()) );
	    	readers.add( consume(process.getErrorStream()) );
	    	Selenium.pause( delay );
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

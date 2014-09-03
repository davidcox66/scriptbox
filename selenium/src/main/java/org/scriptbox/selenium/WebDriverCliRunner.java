package org.scriptbox.selenium;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class WebDriverCliRunner {

    private String name;
    private String screenshotFileName;
    private String profile;
    private DriverFactory factory;
    private URL remoteUrl;
     
    public WebDriverCliRunner(String name, String[] args) {
        this.name = name;
        
    	int i=0; 
    	for( ; i < args.length ; i++ ) {
           if( args[i].startsWith("--") ) {
                   if( "--profile".equals(args[i]) ) {
                       if( args.length > i+1 ) {
                           profile = args[++i];
                       }
                       else {
                           usage();
                       }
                   }
                   else if( "--firefox".equals(args[i]) )  {
                     factory = FIREFOX; 
                   }
                   else if( "--chrome".equals(args[i]) ) {
                      factory = CHROME; 
                   }
                   else if( "--ie".equals(args[i]) ) {
                      factory = IE; 
                   }
                   else if( "--screenshot".equals(args[i]) ) {
                       if( args.length > i+1 ) {
                           screenshotFileName = args[++i]; 
                       }
                       else {
                           usage();
                       }
                   }
                   else if( "--remote-url".equals(args[i]) ) {
                       if( args.length > i+1 ) {
                           try {
	                           remoteUrl = new URL(args[++i]); 
                           }
                           catch( Exception ex ) {
                               System.err.println( "Invalid remote url" );
                               ex.printStackTrace();
                               usage();
                           }
                       }
                       else {
                           usage();
                       }
                   }
                   else {
                     usage(); 
                  }
               }
               else {
                   break;
               }
        }
          
    	if( factory == null ) {
    	    usage();
    	}
        	
    }
    
    public boolean execute( WebDriverTemplate template ) {
        boolean ret = true;
        RemoteWebDriver driver = null;
        SeleniumMethods selenium = null;
        try {
        	System.err.println( "Using profile: " + profile );
	       // DesiredCapabilities cap = DesiredCapabilities.firefox();
	       // cap.setCapability( FirefoxDriver.PROFILE, args[0] );
	       // RemoteWebDriver driver = new FirefoxDriver(cap);
        
           // ProfilesIni allProfiles = new ProfilesIni();
           // FirefoxProfile profile = allProfiles.getProfile(args[0]);
        	// RemoteWebDriver driver = new FirefoxDriver(profile);
        	
	        // RemoteWebDriver driver = new ChromeDriver();
	        // RemoteWebDriver driver = new RemoteWebDriver(new URL("http://localhost:9515"), DesiredCapabilities.chrome());
       
       
        	driver = factory.create();
        	// if( screenshotFile != null ) {
        	//    driver = (RemoteWebDriver)new Augmenter().augment(driver);
        	// }
        
        	driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        	selenium = new SeleniumMethods( factory.getName() );
        	selenium.setDriver( driver );
        	template.execute( selenium );
        }
        catch(Exception ex ) {
            ex.printStackTrace();
            ret = false;
        }
       
        if( screenshotFileName != null && selenium != null ) {
            try {
                selenium.screenshot( new File(screenshotFileName) );
            }
            catch( Exception ex ) {
                System.err.println( "Failed taking screenshot" );
                ex.printStackTrace();
                ret = false;
            }
        }
        
        if( driver != null ) {
            System.err.println( "Quitting driver..." );
            try {
	            driver.quit();
            }
            catch( Exception ex ) {
                System.err.println( "Failed shutting down" );
                ex.printStackTrace();
                ret = false;
            }
        }
        System.err.println( "Done." );
        return ret;
    }
   
    private void usage() {
        System.err.println( "Usage: " + name + "{--firefox [--profile <profile path>] | --chrome [--remote-url <url>]} [--screenshot <file name minus ext>]");
        System.exit( 1 );
    }
    
    interface DriverFactory  {
        public String getName();
        public RemoteWebDriver create(); 
    }
    
    private DriverFactory FIREFOX = new DriverFactory() {
        public String getName() { return "firefox"; }
        public RemoteWebDriver create() {
            if( remoteUrl != null ) {
	           DesiredCapabilities cap = DesiredCapabilities.firefox();
	           if( profile != null ) {
		           cap.setCapability(FirefoxDriver.PROFILE, profile);
	           }
		       RemoteWebDriver driver = new RemoteWebDriver(remoteUrl, cap);
		       return (RemoteWebDriver)new Augmenter().augment(driver);
            }
            else {
		        File profileDir = new File(profile);
		        return new FirefoxDriver( new FirefoxProfile(profileDir) ); 
            }
        }
    };
    
    private DriverFactory CHROME = new DriverFactory() {
        public String getName() { return "chrome"; }
        public RemoteWebDriver create() {
           // return new ChromeDriver(); 
           DesiredCapabilities cap = DesiredCapabilities.chrome();
           cap.setCapability("chrome.switches", Arrays.asList("--ignore-certificate-errors"));
	       RemoteWebDriver driver = new RemoteWebDriver(remoteUrl, cap);
           return (RemoteWebDriver)new Augmenter().augment(driver);
        }
    };
    private DriverFactory IE = new DriverFactory() {
        public String getName() { return "ie"; }
        public RemoteWebDriver create() {
           DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
           cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);   
	       RemoteWebDriver driver = new RemoteWebDriver(remoteUrl, cap);
           return (RemoteWebDriver)new Augmenter().augment(driver);
        }
    };
}

package org.scriptbox.selenium;

import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;

public class WebDriverCliRunner {

    private String name;
    private String screenshotFileName;
    private String profile;
    private DriverType type;
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
                     type = DriverType.FIREFOX;
                   }
                   else if( "--chrome".equals(args[i]) ) {
                     type = DriverType.CHROME;
                   }
                   else if( "--ie".equals(args[i]) ) {
                     type = DriverType.IE;
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
          
    	if( type == null ) {
    	    usage();
    	}
        	
    }
    
    public boolean execute( WebDriverTemplate template ) {
        boolean ret = true;
        RemoteWebDriver driver = null;
        SeleniumService selenium = null;
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
       
       
        	// driver = factory.create();
        	// if( screenshotFile != null ) {
        	//    driver = (RemoteWebDriver)new Augmenter().augment(driver);
        	// }
        
            SeleniumController.setInstance( new SeleniumController(type) );
            selenium = new DriverSeleniumService();
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
}

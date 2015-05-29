package com.lrd.selenium;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.scriptbox.selenium.*;
import org.scriptbox.selenium.driver.DriverSeleniumService;
import org.scriptbox.selenium.driver.DriverType;
import org.scriptbox.selenium.driver.SeleniumController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This controls the lifecycle (or execution) of a test which uses Selenium to drive one more
 * more browsers through unit tests. SeleniumRunner manages the connection to selenium 
 * servers, both in creating the connections and shutting them down properly. Individual
 * tests only need to supply a setSelenium(SeleniumHelper) method to inject the object
 * to communicate with the Selenium server.
 * 
 * This Runner is actually composed of multiple child runners - one for each browser. Tests
 * will be run as a test suite. Each runner in the suite is responsible for running tests
 * in one particular browser. Test exclusions can be specified by the use of the
 * {@link Browser} annotation.
 *
 * SeleniumRunner also adds screenshot functionality to tests. Any test can specify that
 * a screenshot be taken at the end for test failures or unconditionally at the end of the
 * test.
 * 
 * @author david
 * @since Jan 12, 2012
 */
public class SeleniumRunner extends ParentRunner<Runner> {

    private static final Logger LOGGER = LoggerFactory.getLogger( SeleniumRunner.class );
  
    /** 
     * The host where the selenium servers are running. Often these server will be connected
     * by an SSH tunnel to the remote machine, in which case the host will be 'localhost' (the default)
     */
    public static final String HOST = System.getProperty( "selenium.host", "localhost" );
    
    /**
     * The port of the chromedriver executable which drives the Chrome browser. The 9515 address
     * cannot be changed though an SSH tunnel can listen to a different local port number and forward
     * to 9515 on the remote end. 
     */
    public static final int BASE_PORT = Integer.parseInt( System.getProperty( "selenium.base_port", "9515") );

    /**
     * The path where screenshots will be saved. 
     */
    public static final File SCREEN_SHOT_DIR = new File( System.getProperty("selenium.screenshot_dir", "target/screenshots") );
   
    /**
     * Instruct the runner not to shutdown the browser at the conclusion of the test so you
     * can tinker/observe whatever the test did.
     */
    public static final boolean NO_QUIT = System.getProperty("selenium.no_quit") != null;

    /**
     * Instruct the runner to use firefox locally.
     */
    public static final boolean FIREFOX_LOCAL = System.getProperty( "selenium.firefox.local" ) != null;
    
    /**
     * A firefox profile which should exist on the remote server
     */
    public static final String FIREFOX_PROFILE_NAME = System.getProperty( "selenium.firefox.profile", "uitest" );
    
    /**
     * Instruct the runner to use a different firefox binary when running locally.
     */
    public static final String FIREFOX_BINARY = System.getProperty( "selenium.firefox.binary" );
   
    /**
     * Allow overriding the default list of browsers to test
     */
    public static final String BROWSERS = System.getProperty("selenium.browsers", "firefox,chrome,ie" );
    
    static {
        LOGGER.debug( "SeleniumRunner settings: " +
            "browsers: " + BROWSERS + 
            ", host: " + HOST + 
            ", base_port: " + BASE_PORT   +
            ", screenhost_dir: " + SCREEN_SHOT_DIR   +
            ", no_quit: " + NO_QUIT +
            ", firefox_local: " + FIREFOX_LOCAL +
            ", firefox_profile_name: " + FIREFOX_PROFILE_NAME +
            ", firefox_binary: " + FIREFOX_BINARY );
    }
    
    private List<Runner> runners = new ArrayList<Runner>();

    public SeleniumRunner( Class<?> testClass ) throws InitializationError {
        super( testClass );
        //
        // Construct test runners for all the specified browser types
        //
        Set<String> browsers = new HashSet<String>( Arrays.asList(BROWSERS.split(",")) );
		for( DriverType dt : DriverType.values() ) {
            if( browsers.contains(dt.getName()) ) {
                runners.add( new BrowserRunner(testClass,dt.getName()) );
            }
        }
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(Runner child) {
		return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
		child.run(notifier);
    }

    /**
     * Run the suite of tests for the class on all specified browsers and shutdown the selenium connections
     * at the conclusion of tests on the target class.
     */
    @Override
	protected Statement classBlock(final RunNotifier notifier) {
        final Statement next = super.classBlock( notifier );
        return new Statement() {
            public void evaluate() throws Throwable {
                try {
	                next.evaluate();
                }
                finally { 
                    disposeAll();
                }
            }
        };
    }
  
    private void disposeAll() {
       for( Runner runner : runners )  {
           ((BrowserRunner)runner).dispose();
       }
    }
    
    private class BrowserRunner extends BlockJUnit4ClassRunner {

		private String driver;
        private SeleniumMethods selenium;
        
        BrowserRunner(Class<?> type, String driver ) throws InitializationError {
            super(type);
			this.driver = driver;
            try {
				SeleniumController controller = new SeleniumController( DriverType.getByName(driver) );
				DriverSeleniumService service = new DriverSeleniumService( controller );
	            selenium = new SeleniumMethods( service );
            }
            catch( Exception ex ) {
                LOGGER.error( "Error initializing driver: " + driver, ex );
                throw new InitializationError( ex );
            }
        }

		@Override
		protected String getName() {
		    return String.format("[%s]", driver );
		}
		
		@Override
		protected String testName(final FrameworkMethod method) {
		    return String.format("%s[%s]", method.getName(), driver );
		}
		
		@Override
		protected void validateConstructor(List<Throwable> errors) {
		    validateOnlyOneConstructor(errors);
		}
		
		@Override
		protected Statement classBlock(RunNotifier notifier) {
		    return childrenInvoker(notifier);
		}
		
		/**
		 * Runs the specified test method and takes a screenshot at the end of the test if specified
		 * through the {@link Screenshot} annotation.
		 */
		@Override
		protected Statement methodBlock(final FrameworkMethod method) {
		    final Statement next = super.methodBlock( method );
	        return new Statement() {
	            public void evaluate() throws Throwable {
	                try {
		                next.evaluate();
	                    takeScreenshotIfSpecified( method, false );
	                }
	                catch( AssertionError ex ) {
	                    takeScreenshotIfSpecified( method, true );
	                    throw ex;
	                }
	                catch( Exception ex ) {
	                    takeScreenshotIfSpecified( method, true );
	                    throw ex;
	                }
	            }
	        };
		}

		/**
		 * Creates the test object and injects its selenium attributes.
		 */
		@Override
		protected Object createTest() throws Exception {
		    Object test = super.createTest();
		    injectAttributes( test );
		    return test;
		}

	
		/**
		 * This has been overridden to add exclusion of browsers based upon a {@link Browser} annotation
		 * on the test method or class.
		 */
		@Override
		protected void runChild(FrameworkMethod method, RunNotifier notifier) {
			EachTestNotifier eachNotifier= makeNotifier(method, notifier);
			if (isBrowserTestIgnored(method) || method.getAnnotation(Ignore.class) != null) {
				runIgnored(eachNotifier);
			} else {
				runNotIgnored(method, eachNotifier);
			}
		}

		// Had to override this to implement browser ignore in runChild()
		private void runNotIgnored(FrameworkMethod method,
				EachTestNotifier eachNotifier) {
			eachNotifier.fireTestStarted();
			try {
				methodBlock(method).evaluate();
			} catch (AssumptionViolatedException e) {
				eachNotifier.addFailedAssumption(e);
			} catch (Throwable e) {
				eachNotifier.addFailure(e);
			} finally {
				eachNotifier.fireTestFinished();
			}
		}
	
		// Had to override this to implement browser ignore in runChild()
		private void runIgnored(EachTestNotifier eachNotifier) {
			eachNotifier.fireTestIgnored();
		}
	
		// Had to override this to implement browser ignore in runChild()
		private EachTestNotifier makeNotifier(FrameworkMethod method,
				RunNotifier notifier) {
			Description description= describeChild(method);
			return new EachTestNotifier(notifier, description);
		}
	

		/**
		 * Evaluates the options specified in the {@link Browser} annotation of a test method or the
		 * test class. An annotation on the method will override that of one on the class.
		 * 
		 * @param method
		 * @return
		 */
		private boolean isBrowserTestIgnored( FrameworkMethod method ) {
	        Browser annotation= method.getMethod().getAnnotation(Browser.class);
	        if( annotation == null ) {
	            annotation = getTestClass().getJavaClass().getAnnotation( Browser.class );
	        }
	        if( annotation != null ) {
	            String browserName = driver;
	            if( annotation.only().length > 0 ) {
	                for( String name : annotation.only() ) {
	                    if( browserName.equals(name) ) {
	                        return false;
	                    }
	                }
	                return true;
	            }
	            else if( annotation.except().length > 0 ) {
	                for( String name : annotation.except() ) {
	                    if( browserName.equals(name) ) {
	                        return true;
	                    }
	                }
	            }
	        }
	        return false;
		}
		
	    private void injectAttributes( Object test ) throws Exception {
	        Method method = test.getClass().getMethod( "setSelenium", new Class[] { GroovySeleniumShell.class } );
	        method.invoke( test, new Object[] { selenium } );
	    }
	  
	    /**
	     * Checks if there is a {@link Screenshot} annotation on the test method or test class.
	     * A screenshot annotation on the method will override one on the class. 
	     * 
	     * @param method
	     * @param failed true if the test method generated an exception
	     */
	    private void takeScreenshotIfSpecified( FrameworkMethod method, boolean failed ) {
	        Screenshot annotation= method.getMethod().getAnnotation(Screenshot.class);
	        if( annotation == null ) {
	            annotation = getTestClass().getJavaClass().getAnnotation( Screenshot.class );
	        }
	        if( annotation != null && (failed || annotation.always()) ) {
	            takeScreenshot( method );
	        }
	    }
	   
	    /**
	     * Generates a screenshot of the browser under test. The file name will be a composite of the
	     * class, method, and browser name.
	     * @param method
	     */
	    private void takeScreenshot( FrameworkMethod method ) {
	        String clsName = getTestClass().getName();
	        int ext = clsName.lastIndexOf(".");
	        if( ext != -1 ) {
	            clsName = clsName.substring(ext+1);
	        }
	        try {
	            LOGGER.info( "takeScreenshot: driver=" + driver + ", method=" + method.getName() );
	            selenium.screenshot( SCREEN_SHOT_DIR, getTestClass().getName(), method.getName(), driver, null );
	        }
	        catch( Exception ex ) {
	            LOGGER.error( "Unable to take screenshot: " +
	                "driver=" + driver +
	                ", class=" + getTestClass().getName() + 
	                ", method=" + method.getName(), ex );
	        }
	    }
	    
		private void dispose() {
		    try {
		        if( !NO_QUIT ) {
			        selenium.quit();
		        }
		    }
		    catch( Exception ex ) {
		        LOGGER.error( "Error quitting browser: " + driver, ex );
		    }
		}
    }

    interface DriverFactory  {
        public String getName();
        public RemoteWebDriver create( String host ) throws Exception; 
    }
       
}

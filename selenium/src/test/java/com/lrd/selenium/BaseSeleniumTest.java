package com.lrd.selenium;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.scriptbox.selenium.SeleniumHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseSeleniumTest {

    private static final Logger LOGGER = LoggerFactory.getLogger( BaseSeleniumTest.class );

    @Rule public TestName methodNameRule = new TestName();
    
    protected SeleniumHelper selenium;

    public SeleniumHelper getSelenium() {
        return selenium;
    }

    public void setSelenium(SeleniumHelper selenium) {
        this.selenium = selenium;
    }

    public void click( String id ) {
        WebElement elem = selenium.getElementById( id );
        elem.click();
    }
    public String getValueAttribute( String id ) {
        WebElement elem = selenium.getElementById(id);
        return elem.getAttribute("value");
    }
    
    public void setText( String id, String value) {
        RemoteWebElement elem = (RemoteWebElement)selenium.getElementById(id);
        elem.clear();
        elem.sendKeys(value + "\n" );
    }
   
    public String getText( String id ) {
        WebElement elem = selenium.getElementById(id);
        return elem.getText();
    }
   
    public DateMidnight getDate( String id ) {
        String text = getText( id );
        return DateTimeFormat.forPattern("dd/MM/yyyy")
	        .withOffsetParsed()
	        .parseDateTime( text )
	        .toDateMidnight();
    }
   
    public List<String> waitForElementValuesBySelector( String selector, int tries ) {
        for( int i=0 ; i < tries ; i++ ) {
            List<String> values = getElementValuesBySelector(selector);
            if( values != null && values.size() > 0 ) {
                return values;
            }
            sleep();
        }
        return new ArrayList<String>();
    }

    private void sleep() {
        try {
            Thread.sleep( 1000 );
        }
        catch( InterruptedException ex ) {
            LOGGER.warn( "assertNoErrorMessages: interrupted", ex );
            throw new RuntimeException( "Interrupted while waiting for selenium", ex );
        }
    }
    
    public List<String> getElementValuesBySelector( String selector ) {
       List<WebElement> elements = selenium.getElementsBySelector( selector );
       List<String> ret = new ArrayList<String>();
       for( WebElement elem : elements ) {
           String text = elem.getText();
           if( StringUtils.isNotBlank(text) ) {
	           ret.add( text );
           }
       }
       return ret;
    }
    
    public void setDate( String id, DateMidnight date ) {
        setDate( id, date.getMonthOfYear(), date.getDayOfMonth(), date.getYear() );
    }
    
    public void setDate( String id, int month, int dayOfMonth, int year ) {
        setText( id, pad(dayOfMonth) + "/" + pad(month)  + "/" + year  );
    }
    private String pad( int val ) {
        return val >= 10 ? (""+ val) : ("0" + val);
    }
    public void screenshot( String ext ) {
        try {
	        selenium.screenshot( SeleniumRunner.SCREEN_SHOT_DIR, getClass().getName(), methodNameRule.getMethodName(), selenium.getName(), ext );
        }
        catch( Exception ex ) {
            LOGGER.error( "Error taking screenshot", ex );
        }
    } 
    
    public String generateRandomCode() {
        return generateRandomCode( 3 );
    }
    public String generateRandomCode( int length ) {
        return generateRandomCode( "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", length );
        
    }
    public String generateRandomCode( final String chars, int length ) {
        Random rand = new Random();
        StringBuilder ret = new StringBuilder();
        for( int i=0 ; i < 3; i++ ) { 
            ret.append( chars.charAt(rand.nextInt(chars.length())) );
        }
        return ret.toString();
    }
}

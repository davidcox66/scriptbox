package org.scriptbox.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Created by david on 5/18/15.
 */
public interface SeleniumService {

    boolean activate( ExpectedCondition<Boolean> cond );

    void connect();

    void disconnect();

    void quit();

    void get(String url);

    void execute(String script, Object... args);

    void executeAsync(String script, Object... args);

    boolean isElementExists(By by);

    WebElement getElement(By by);

    List<WebElement> getElements(By by);

    WebElement waitForElement(By by, int seconds);

    String waitForNewWindow(int seconds);

    <X> X waitFor(int seconds, ExpectedCondition<X> cond);

    WebElement clickElement(By by, int seconds);

    WebElement moveToBy(By by, int seconds);

    WebElement moveToElement(WebElement element, int seconds);

    void mouseDown(WebElement element);
    void mouseUp(WebElement element);
    void mouseMove(WebElement element);
    void mouseClick(WebElement element);
    void mouseDoubleClick(WebElement element);
    void mouseContextClick(WebElement element);

    void screenshot(File file) throws IOException;

    void click( WebElement element );
    void submit( WebElement element );
    void sendKeys( WebElement element, CharSequence... keysToSend );
    void clear( WebElement element );
    String getTagName( WebElement element );
    String getAttribute( WebElement element, String name );
    String getText( WebElement element );
    List<WebElement> findElements( WebElement element, By by );
    WebElement findElement( WebElement element, By by );
    String getCssValue( WebElement element, String propertyName );
    boolean isDisplayed( WebElement element );
    boolean isSelected( WebElement element );
    boolean isEnabled( WebElement element );
    Point getLocation( WebElement element );
    Dimension getSize( WebElement element );

    String getCurrentUrl();
    String getWindowHandle();

    void closeWindow();
    void switchToFrameByIndex( int index );
    void switchToFrameByNameOrId( String nameOrId );
    void switchToFrameByElement( WebElement element );
    void switchToWindow( String nameOrHandle );
    void switchToDefaultContent();
    WebElement switchToActiveElement();

    void back();
    void forward();
    void to(URL url);
    void refresh();
}

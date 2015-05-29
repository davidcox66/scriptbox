package org.scriptbox.selenium;

import groovy.lang.Binding;
import org.scriptbox.selenium.bind.BindUtils;

/**
 * Created by david on 5/18/15.
 */
public class GroovySeleniumBinder {

   private Binding binding;
   private Object instance;

   public GroovySeleniumBinder( Binding binding, Object instance ) {
      this.binding = binding;
      this.instance = instance;
   }

     void bind() {
        bind( "get" );
        bind( "screenshot" );
        bind( "execute" );
        bind( "executeAsync" );

        bind( "isElementExists" );
        bind( "isElementExistsById" );
        bind( "isElementExistsByName" );
        bind( "isElementExistsByXpath" );
        bind( "isElementExistsByCss" );

        bind( "getElement" );
        bind( "getElements" );
        bind( "getElementById" );
        bind( "getElementByName" );
        bind( "getElementByXpath" );
        bind( "getElementsByXpath" );
        bind( "getElementByCss" );
        bind( "getElementsByCss" );

        bind( "waitFor" );
        bind( "waitForElement" );
        bind( "waitForElementById" );
        bind( "waitForElementByName" );
        bind( "waitForElementByXpath" );
        bind( "waitForElementByCss" );

        bind( "clickElement" );
        bind( "clickElementById" );
        bind( "clickElementByName" );
        bind( "clickElementByXpath" );
        bind( "clickElementByCss" );

        bind( "moveToElement" );
        bind( "moveToElementById" );
        bind( "moveToElementByName" );
        bind( "moveToElementByXpath" );
        bind( "moveToElementByCss" );

        bind( "withNewWindow" );
        bind( "waitForNewWindow" );

        bind( "mouseDown" );
        bind( "mouseUp" );
        bind( "mouseMove" );
        bind( "mouseClick" );
        bind( "mouseDoubleClick" );
        bind( "mouseContextClick" );

        bind( "byId" );
        bind( "byName" );
        bind( "byIdOrName" );
        bind( "byCss" );
        bind( "byLinkText" );
        bind( "byPartialLinkText" );
        bind( "byTagName" );
        bind( "byXpath" );
        bind( "byAll" );
        bind( "byAny" );
        bind( "byChained" );

        bind( "presenceOf" );
        bind( "presenceOfAll" );
        bind( "visibilityOf" );
        bind( "visibilityOfAll" );
        bind( "invisibilityOf" );
        bind( "invisibilityOfAll" );
        bind( "textPresent" );
        bind( "valuePresent" );
        bind( "clickable" );
        bind( "clickableAny" );
        bind( "clickableAll" );
        bind( "selected" );

        bind( "activate" );
        bind( "connect" );
        bind( "disconnect" );
        bind( "quit" );
        bind( "baseUrl" );
        bind( "urlPattern" );

        bind( "switchToFrameByIndex" );
        bind( "switchToFrameByNameOrId" );
        bind( "switchToFrameByElement" );
        bind( "switchToWindow" );
        bind( "switchToDefaultContent" );
        bind( "switchToActiveElement" );
        bind( "closeWindow" );

        bind( "back" );
        bind( "forward" );
        bind( "to" );
        bind( "refresh" );

        bind( "select" );

        bind( "sleep" );
        bind( "pause" );
    }

    public void bind( String name ) {
         BindUtils.bind(binding, instance, name);
    }

    public void bind( String alias, String name ) {
         BindUtils.bind( binding, instance, alias, name );
    }

}

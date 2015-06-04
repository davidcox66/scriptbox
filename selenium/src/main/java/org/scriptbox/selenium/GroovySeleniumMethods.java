package org.scriptbox.selenium;

import groovy.lang.Binding;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.bind.Bindable;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class GroovySeleniumMethods extends SeleniumMethods implements Bindable, SeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger( GroovySeleniumMethods.class );

	private static final AtomicInteger counter = new AtomicInteger();

    private Binding binding;

	public GroovySeleniumMethods() {
	}

    public GroovySeleniumMethods(SeleniumService service)  {
		super( service );
    }

	public void init( SeleniumExtensionContext ctx ) {
		setService( ctx.getService() );
		bind( ctx.getBinding() );
	}

	public void bind( Binding binding ) {
		this.binding = binding;

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

		bind( "getCurrentUrl" );
		bind( "getWindows" );
		bind( "openWindow" );
		bind( "closeWindow" );
		bind( "closeCurrentWindow" );

		bind( "back" );
		bind( "forward" );
		bind( "to" );
		bind( "refresh" );

		bind( "select" );

		bind( "sleep" );
		bind( "pause" );

		Logger logger = LoggerFactory.getLogger( "SeleniumScript" );
		binding.setVariable("log", logger);
		binding.setVariable("logger", logger);
		binding.setVariable("LOGGER", logger);
	}

	public void bind( String name ) {
		BindUtils.bind(binding, this, name);
	}

	public void bind( String alias, String name ) {
		BindUtils.bind( binding, this, alias, name );
	}

}

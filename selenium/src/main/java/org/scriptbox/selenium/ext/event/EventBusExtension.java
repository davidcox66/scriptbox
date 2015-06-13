package org.scriptbox.selenium.ext.event;

import groovy.lang.Binding;
import groovy.lang.Closure;
import org.scriptbox.selenium.bind.BindUtils;
import org.scriptbox.selenium.ext.AbstractSeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtension;
import org.scriptbox.selenium.ext.SeleniumExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 5/29/15.
 */
public class EventBusExtension extends AbstractSeleniumExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger( EventBusExtension.class );

    private EventBus bus;

    public void configure() throws Exception {
        Binding binding = context.getBinding();
        BindUtils.bind(binding, this, "observe");
        BindUtils.bind(binding, this, "subscribe");
        BindUtils.bind(binding, this, "unsubscribe");
        BindUtils.bind(binding, this, "raise");
        BindUtils.bind(binding, this, "event");
    }

    public void observe( Object event, Closure listener ) {
        subscribe( event, listener );
    }

    public void subscribe( Object event, Closure listener ) {
        getBus().subscribe(event, listener);
    }

    public void unsubscribe( Object event, Closure listener ) {
        getBus().unsubscribe(event, listener);
    }
    public void raise( final Object event, final Object... args ) {
        getBus().raise(event, args);
    }

    public Event event( Object... args ) {
        return new Event( args );
    }

    synchronized private EventBus getBus()  {
        if( bus == null ) {
            bus = new EventBus();
        }
        return bus;
    }
}

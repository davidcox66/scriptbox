package org.scriptbox.panopticon.events;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventsPlugin.class);

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(EventslInjector.class));
		super.contextCreated(context);
		context.getBeans().put("EventBus", new EventBus(context) ); 
	}

	public void subscribe( Object event, ParameterizedRunnable<Object> runnable ) {
		EventBus bus = BoxContext.getCurrentContext().getBeans().get("EventBus",EventBus.class); 
		bus.subscribe( event, runnable );
	}

	public void unsubscribe( Object event, ParameterizedRunnable<Object> runnable ) {
		EventBus bus = BoxContext.getCurrentContext().getBeans().get("EventBus",EventBus.class); 
		bus.unsubscribe( event, runnable );
	}
	
	public void raise( Object...args ) {
		EventBus bus = BoxContext.getCurrentContext().getBeans().get("EventBus",EventBus.class); 
		bus.raise( args );
	}
}

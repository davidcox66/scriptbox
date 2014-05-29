package org.scriptbox.panopticon.inbox;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboxPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(InboxPlugin.class);

	public InboxPlugin() {
	}
	
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(InboxInjector.class));
		super.contextCreated(context);
	}

	@Override
	public void contextShutdown(BoxContext context) throws Exception {
		super.contextShutdown(context);
	}
}

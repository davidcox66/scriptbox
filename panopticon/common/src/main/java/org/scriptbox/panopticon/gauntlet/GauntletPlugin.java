package org.scriptbox.panopticon.gauntlet;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GauntletPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(GauntletPlugin.class);

	public GauntletPlugin() {
	}
	
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(GauntletInjector.class));
		super.contextCreated(context);
	}

	@Override
	public void contextShutdown(BoxContext context) throws Exception {
		super.contextShutdown(context);
	}
}

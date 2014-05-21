package org.scriptbox.panopticon.mail;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MailPlugin.class);

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(MailInjector.class));
		super.contextCreated(context);
	}

}

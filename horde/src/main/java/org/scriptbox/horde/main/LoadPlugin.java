package org.scriptbox.horde.main;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class LoadPlugin extends BoxContextInjectingListener {

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(LoadInjector.class) );
		super.contextCreated( context );
	}
}

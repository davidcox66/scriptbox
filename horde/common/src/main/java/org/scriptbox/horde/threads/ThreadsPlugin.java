package org.scriptbox.horde.threads;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class ThreadsPlugin extends BoxContextInjectingListener {

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(ThreadsInjector.class) );
		super.contextCreated( context );
	}
	@Override
	public void contextShutdown(BoxContext context) throws Exception {
		super.contextShutdown( context );
	}
}

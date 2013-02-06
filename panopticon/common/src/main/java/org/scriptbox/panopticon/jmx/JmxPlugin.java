package org.scriptbox.panopticon.jmx;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class JmxPlugin extends BoxContextInjectingListener {

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(JmxInjector.class) );
		super.contextCreated( context );
	}
	
}

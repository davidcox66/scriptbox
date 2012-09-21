package org.scriptbox.horde.action;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class ActionPlugin extends BoxContextInjectingListener {

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(ActionInjector.class) );
		super.contextCreated( context );
	}
}

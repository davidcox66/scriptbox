package org.scriptbox.box.container;

import org.scriptbox.box.container.Box;
import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.BoxContextFactory;

public class BasicBoxContextFactory implements BoxContextFactory {

	@Override
	public BoxContext create( Box box, String language, String contextName) {
		return new BoxContext( box, language, contextName );
	}

}

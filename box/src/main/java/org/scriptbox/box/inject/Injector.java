package org.scriptbox.box.inject;

import org.scriptbox.box.container.BoxContext;

public interface Injector {

	public String getLanguage();
	public void inject( BoxContext context );
}

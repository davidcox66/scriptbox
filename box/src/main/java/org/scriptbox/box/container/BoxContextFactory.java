package org.scriptbox.box.container;


public interface BoxContextFactory {

	public BoxContext create( Box box, String language, String contextName );
}

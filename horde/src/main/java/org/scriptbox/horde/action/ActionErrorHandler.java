package org.scriptbox.horde.action;

public interface ActionErrorHandler {

	public void handle( Throwable ex ) throws Throwable;
}

package org.scriptbox.box.events;

import org.scriptbox.box.container.Box;

public interface BoxListener {

	public void createdBox( Box box ) throws Exception;
	public void shutdownBox( Box box ) throws Exception;
}

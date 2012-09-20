package org.scriptbox.box.controls;

import org.scriptbox.box.container.StatusProvider;

public interface BoxService extends StatusProvider {

	public void start() throws Exception;
	public void stop() throws Exception;
	public void shutdown() throws Exception;
}

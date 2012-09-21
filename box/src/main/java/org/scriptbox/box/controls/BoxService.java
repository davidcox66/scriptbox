package org.scriptbox.box.controls;

import java.util.List;

import org.scriptbox.box.container.StatusProvider;

public interface BoxService extends StatusProvider {

	public void start( List arguments ) throws Exception;
	public void stop() throws Exception;
	public void shutdown() throws Exception;
}

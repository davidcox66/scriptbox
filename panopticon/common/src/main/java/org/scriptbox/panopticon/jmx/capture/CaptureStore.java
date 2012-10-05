package org.scriptbox.box.plugins.jmx.capture;

public interface CaptureStore {

	public void store( CaptureResult result ) throws Exception;
}

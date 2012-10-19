package org.scriptbox.panopticon.capture;


public interface CaptureStore {

	public void store( CaptureResult result ) throws Exception;
	public void flush() throws Exception;
}

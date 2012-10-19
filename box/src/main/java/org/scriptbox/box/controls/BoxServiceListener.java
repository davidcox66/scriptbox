package org.scriptbox.box.controls;

import java.util.List;

public interface BoxServiceListener {

	public void starting( List arguments ) throws Exception;
	public void stopping() throws Exception;
	public void shutdown() throws Exception;
}

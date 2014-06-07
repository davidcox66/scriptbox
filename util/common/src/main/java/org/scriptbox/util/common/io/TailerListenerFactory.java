package org.scriptbox.util.common.io;

import java.io.File;

public interface TailerListenerFactory {

	public TailerListener create( File file );
}

package org.scriptbox.util.common.io;

import java.io.File;

import org.apache.commons.io.input.TailerListener;

public interface TailerListenerFactory {

	public TailerListener create( File file );
}

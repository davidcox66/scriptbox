package org.scriptbox.panopticon.jmx;

import java.util.Collection;
import org.scriptbox.panopticon.capture.CaptureResult;

public interface Storable {

	public Collection<CaptureResult> getResults();
}

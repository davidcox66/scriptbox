package org.scriptbox.box.controls;

import java.util.Collection;


import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.collection.Iterators;
import org.scriptbox.util.common.error.Errors;

public class BoxServices {

	public static Errors<BoxService> start( Collection<BoxService> services ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				service.start();
			}
		} );
	}
	
	public static Errors<BoxService> stop( Collection<BoxService> services ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				service.stop();
			}
		} );
	}
	public static Errors<BoxService> shutdown( Collection<BoxService> services ) throws Exception {
		return Iterators.callAndCollectExceptions( services, new ParameterizedRunnable<BoxService>() {
			public void run( BoxService service ) throws Exception {
				service.shutdown();
			}
		} );
	}
	
}

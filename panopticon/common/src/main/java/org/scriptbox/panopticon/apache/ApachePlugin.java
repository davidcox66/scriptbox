package org.scriptbox.panopticon.apache;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.box.jmx.proc.GenericProcess;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApachePlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApachePlugin.class);

	private static GenericProcess PROC = new GenericProcess("System");

	private CaptureStore store;

	public CaptureStore getStore() {
		return store;
	}

	public void setStore(CaptureStore store) {
		this.store = store;
	}

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(ApacheInjector.class));
		super.contextCreated(context);
	}
	
	public void status( String url ) {
	}
	
	public void balancer( String location ) {
		
	}
	public void log( String location, final ParameterizedRunnableWithResult<String,String> closure ) {
		
	}
	public void reset( String processPattern ) {
		
	}
}

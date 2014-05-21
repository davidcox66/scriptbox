package org.scriptbox.panopticon.tail;

import java.util.List;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.controls.BoxServiceListener;
import org.scriptbox.box.inject.BoxContextInjectingListener;
import org.scriptbox.util.common.io.FileSource;
import org.scriptbox.util.common.io.MultiTailer;
import org.scriptbox.util.common.io.TailerListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TailPlugin extends BoxContextInjectingListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(TailPlugin.class);

	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors(context.getBox().getInjectorsOfType(TailInjector.class));
		super.contextCreated(context);
	}

	public void tail( int interval, FileSource source, TailerListenerFactory factory ) {
		final MultiTailer reader = new MultiTailer( interval, source, factory );
		// Add this as a BoxServiceListener to get notification of stopping and
		// shutdown
		BoxContext.getCurrentContext().getBeans().put(reader.toString(), new BoxServiceListener() {
			public void starting(List arguments) throws Exception {
				reader.start();
			}
			public void shutdown() throws Exception {
				reader.stop();
			}
			public void stopping() throws Exception {
				reader.stop();
			}
		});
	}

}

package org.scriptbox.box.plugins.jmx;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.inject.BoxContextInjectingListener;

public class JmxPlugin extends BoxContextInjectingListener {

	private JmxConnections connections;
	
	@Override
	public void contextCreated(BoxContext context) throws Exception {
		setInjectors( context.getBox().getInjectorsOfType(JmxInjector.class) );
		super.contextCreated( context );
	}

	public JmxConnections getConnections() {
		return connections;
	}

	public void setConnections(JmxConnections connections) {
		this.connections = connections;
	}
	
}

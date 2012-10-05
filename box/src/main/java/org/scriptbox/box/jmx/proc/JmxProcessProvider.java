package org.scriptbox.box.jmx.proc;

import org.scriptbox.box.exec.AbstractExecBlock;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnections;

public abstract class JmxProcessProvider extends AbstractExecBlock<ExecRunnable> {

	private String name;
	protected JmxConnections connections;
	
	public JmxProcessProvider( JmxConnections connections, String name ) {
		this.connections = connections;
		this.name = name;
	}

	public JmxConnections getConnections() {
		return connections;
	}

	public String getName() {
		return name;
	}
	
}

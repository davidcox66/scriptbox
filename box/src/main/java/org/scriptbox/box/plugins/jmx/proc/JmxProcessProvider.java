package org.scriptbox.box.plugins.jmx.proc;

import org.scriptbox.box.exec.AbstractExecBlock;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.plugins.jmx.JmxPlugin;

public abstract class JmxProcessProvider extends AbstractExecBlock<ExecRunnable> {

	private String name;
	protected JmxPlugin plugin;
	
	public JmxProcessProvider( JmxPlugin plugin, String name ) {
		this.plugin = plugin;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}

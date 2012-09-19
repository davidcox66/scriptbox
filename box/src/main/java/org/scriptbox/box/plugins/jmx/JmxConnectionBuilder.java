package org.scriptbox.box.plugins.jmx;


public interface JmxConnectionBuilder {

	public JmxConnection getForPid( int pid, String command ) throws Exception;
}

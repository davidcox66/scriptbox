package org.scriptbox.box.jmx.conn;


public interface JmxConnectionBuilder {

	public JmxConnection getForPid( int pid, String command ) throws Exception;
}

package org.scriptbox.box.jmx.opt;

import org.scriptbox.box.jmx.conn.JmxConnection;

public interface BatchRequestElement {

	public void process( JmxConnection connection ); 
}

package org.scriptbox.panopticon.capture;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.GenericProcess;
import org.scriptbox.plugins.jmx.MBeanProxy;

public class CaptureContext {

	private GenericProcess process;
	private JmxConnection connection;
	private ObjectName objectName;
	private Attribute attribute;

	public CaptureContext(GenericProcess process, JmxConnection connection, ObjectName objectName, Attribute attribute) {
		this.process = process;
		this.connection = connection;
		this.objectName = objectName;
		this.attribute = attribute;
	}

	public GenericProcess getProcess() {
		return process;
	}

	public JmxConnection getConnection() {
		return connection;
	}

	public ObjectName getObjectName() {
		return objectName;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public MBeanServerConnection getMBeanServerConnection() throws Exception {
		return connection.getServer();
	}

	public MBeanProxy getMbean() {
		return new MBeanProxy(connection, objectName);
	}

}

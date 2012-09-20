package org.scriptbox.box.plugins.jmx.capture;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.scriptbox.box.plugins.jmx.JmxConnection;
import org.scriptbox.plugins.jmx.MBeanProxy;

public class CaptureContext {

	private JmxConnection connection;
	private ObjectName objectName;
	private Attribute attribute;

	public CaptureContext(JmxConnection connection, ObjectName objectName, Attribute attribute) {
		this.connection = connection;
		this.objectName = objectName;
		this.attribute = attribute;
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

package org.scriptbox.box.jmx.query;

import javax.management.Attribute;
import javax.management.ObjectName;

public interface MBeanQueryHandler {

	public void process( ObjectName objectName, Attribute attribute ) throws Exception;
}

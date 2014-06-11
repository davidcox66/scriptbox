package org.scriptbox.box.jmx.conn;

import java.util.Collection;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.QueryExp;

public abstract class JmxConnection {

	public abstract Object getAttribute( ObjectName objectName, String attribute ) throws Exception;
	public abstract AttributeList getAttributes( ObjectName objectName, Collection attributes ) throws Exception;
	public abstract MBeanAttributeInfo[] getAttributeInfo( ObjectName objectName ) throws Exception;
	public abstract Set<ObjectName> queryNames( ObjectName objectName, QueryExp expression ) throws Exception;
	public abstract Object invoke( ObjectName objectName, String methodName, Object[] arguments, String[] signature ) throws Exception;
	
}

package org.scriptbox.box.jmx.conn;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import java.util.Collection;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

public abstract class JmxConnection {

	public abstract Object getAttribute( ObjectName objectName, String attribute ) 
		throws IOException, ReflectionException, InstanceNotFoundException, AttributeNotFoundException, MBeanException;
	public abstract AttributeList getAttributes( ObjectName objectName, Collection attributes ) 
		throws IOException, ReflectionException, InstanceNotFoundException;
	public abstract MBeanAttributeInfo[] getAttributeInfo( ObjectName objectName )
		throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException;
	public abstract Set<ObjectName> queryNames( ObjectName objectName, QueryExp expression )
		throws IOException; 
	public abstract Object invoke( ObjectName objectName, String methodName, Object[] arguments, String[] signature )
		throws InstanceNotFoundException, MBeanException, ReflectionException, IOException;
	
}

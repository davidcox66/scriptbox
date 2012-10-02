package org.scriptbox.horde.metrics.mbean;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.scriptbox.horde.action.Action;
import org.scriptbox.horde.action.ActionScript;
import org.scriptbox.horde.metrics.AbstractMetric;
import org.scriptbox.horde.metrics.ActionMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDynamicMetricMBean implements DynamicMBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractDynamicMetricMBean.class );
	
    private Map<String,AbstractMetric> metrics = new HashMap<String,AbstractMetric>();
    
    public AbstractDynamicMetricMBean() {
    }

    public abstract ObjectName getObjectName();
    
    public void register() throws Exception {
        ObjectName objName = getObjectName();
        LOGGER.debug( "register: registering mbean objectName=" + objName + ", class=" + getClass());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(this, objName);
        
    }    

    public void unregister() throws Exception {    
        ObjectName objName = getObjectName();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if( mbs.isRegistered(objName) ) {
            LOGGER.debug( "unregister: unregistering mbean objectName=" + objName + ", class=" + getClass());
	        mbs.unregisterMBean( objName );
        }
        else {
            LOGGER.debug( "unregister: not registered mbean objectName=" + objName + ", class=" + getClass());
        }
    }
   
    synchronized public void addMetric( AbstractMetric metric ) {
    	metrics.put( metric.getName(), metric );
    }
    
    synchronized public void removeMetric( ActionMetric metric ) {
    	metrics.remove( metric.getName() );
    }
    
    synchronized public Object getAttribute(String name) throws AttributeNotFoundException {
    	AbstractMetric metric = metrics.get( name );
        if (metric != null) {
            return metric.getValue();
        }
        else {
            throw new AttributeNotFoundException("No such property: " + name);
        }
    }

    public void setAttribute(Attribute attribute)
        throws InvalidAttributeValueException, MBeanException, AttributeNotFoundException 
	{
    }

    synchronized public AttributeList getAttributes(String[] names) {
        AttributeList list = new AttributeList();
        for (String name : names) {
        	AbstractMetric metric = metrics.get( name );
        	if (metric != null) {
        		list.add( new Attribute(name,metric.getValue()) );
        	}
        }
        return list;
    }

    public AttributeList setAttributes(AttributeList list) {
    	return new AttributeList();
    }

    public Object invoke(String name, Object[] args, String[] sig)
        throws MBeanException, ReflectionException 
    {
        throw new ReflectionException(new NoSuchMethodException(name));
    }
    
    synchronized public MBeanInfo getMBeanInfo() {
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[metrics.size()];
        int i=0;
        for( AbstractMetric metric : metrics.values() ) {
            attrs[i++] = new MBeanAttributeInfo(
                    metric.getName(),
                    "java.lang.Integer",
                    metric.getDescription(),
                    true,   // isReadable
                    false,   // isWritable
                    false); // isIs
        }
        MBeanOperationInfo[] opers = new MBeanOperationInfo[] { };
        return new MBeanInfo(
                this.getClass().getName(),
                "Generator Metrics",
                attrs,
                null,  // constructors
                opers,
                null); // notifications
    }
}

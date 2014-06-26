package org.scriptbox.metrics.beans.mbean;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.scriptbox.metrics.beans.AbstractMetric;
import org.scriptbox.metrics.beans.RecordableMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDynamicExposableMBean implements DynamicMBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractDynamicExposableMBean.class );
	
    private Map<String,Exposable> exposables = new HashMap<String,Exposable>();
    
    public AbstractDynamicExposableMBean() {
    }

    public abstract ObjectName getObjectName();
    
    public void register() throws Exception {
        ObjectName objName = getObjectName();
        LOGGER.debug( "register: registering mbean objectName=" + objName + ", class=" + getClass());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if( mbs.isRegistered(objName) ) {
            LOGGER.warn( "register: unregistering existing mbean objectName=" + objName + ", class=" + getClass());
	        mbs.unregisterMBean( objName );
        }
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
   
    synchronized public void addExposable( Exposable exposable ) {
    	exposables.put( exposable.getName(), exposable );
    }
    
    synchronized public void removeExposable( Exposable exposable ) {
    	exposables.remove( exposable.getName() );
    }
    
    synchronized public Object getAttribute(String name) throws AttributeNotFoundException {
    	Exposable exposable = exposables.get( name );
        if (exposable != null) {
            return exposable.getValue();
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
        	Exposable exposable = exposables.get( name );
        	if (exposable != null) {
        		list.add( new Attribute(name,exposable.getValue()) );
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
        MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[exposables.size()];
        int i=0;
        for( Exposable exposable : exposables.values() ) {
            attrs[i++] = new MBeanAttributeInfo(
                    exposable.getName(),
                    "java.lang.Float",
                    exposable.getDescription(),
                    true,   // isReadable
                    false,   // isWritable
                    false); // isIs
        }
        MBeanOperationInfo[] opers = new MBeanOperationInfo[] { };
        return new MBeanInfo(
                this.getClass().getName(),
                "Metrics",
                attrs,
                null,  // constructors
                opers,
                null); // notifications
    }
}

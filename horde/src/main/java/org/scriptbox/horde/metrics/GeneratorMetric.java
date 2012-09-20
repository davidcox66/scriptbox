package org.scriptbox.horde.metrics;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.scriptbox.box.container.BoxScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GeneratorMetric implements GeneratorMetricMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger( GeneratorMetric.class );
    
    protected BoxScript script;
    
    public abstract String getName();
    
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
   
    public String toString() { 
        return getClass().getName() + "{ objectName=" + getObjectName() + " }";
    }
}

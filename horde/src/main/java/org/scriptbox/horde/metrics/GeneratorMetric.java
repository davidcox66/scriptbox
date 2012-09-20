package com.ihg.atp.crs.loadgen.metrics

import java.lang.management.ManagementFactory

import javax.management.MBeanServer
import javax.management.ObjectName

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ihg.atp.crs.loadgen.main.LoadScript


abstract class GeneratorMetric implements GeneratorMetricMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger( GeneratorMetric );
    
    protected LoadScript script;
    
    abstract String getName();
    
    abstract ObjectName getObjectName(); 
    
    void register() {
        ObjectName objName = objectName;
        LOGGER.debug( "register: registering mbean objectName=${objName}, class=${getClass()}");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(this, objName);
        
    }    

    void unregister() {    
        ObjectName objName = objectName;
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if( mbs.isRegistered(objName) ) {
            LOGGER.debug( "unregister: unregistering mbean objectName=${objName}, class=${getClass()}");
	        mbs.unregisterMBean( objName );
        }
        else {
            LOGGER.debug( "unregister: not registered mbean objectName=${objName}, class=${getClass()}");
        }
    }
   
    public String toString() { 
        return getClass().getName() + "{ objectName=${objectName} }";
    }
}

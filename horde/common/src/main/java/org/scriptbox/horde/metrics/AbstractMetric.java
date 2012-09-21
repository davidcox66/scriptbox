package org.scriptbox.horde.metrics;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.scriptbox.horde.action.ActionScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractMetric implements AbstractMetricMBean {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractMetric.class );
    
    protected ActionScript script;
    
    public abstract String getName();
    
    public abstract ObjectName getObjectName(); 
    
    public static void registerAll( Map<ObjectName,? extends AbstractMetric> metrics ) throws Exception {
    	if( metrics != null ) {
    		registerAll( metrics.values() );
    	}
    }
    public static void registerAll( Collection<? extends AbstractMetric> metrics ) throws Exception {
    	if( metrics != null ) {
    		for( AbstractMetric metric : metrics ) {
    			metric.register();
    		}
    	}
    }
    
    public static void unregisterAll( Map<ObjectName,? extends AbstractMetric> metrics ) {
    	if( metrics != null ) {
    		unregisterAll( metrics.values() );
    	}
    }
    public static void unregisterAll( Collection<? extends AbstractMetric> metrics ) {
    	if( metrics != null ) {
    		for( AbstractMetric metric : metrics ) {
    			try {
	    			metric.unregister();
                }
                catch( Exception ex ) {
                    LOGGER.warn( "unregisterAll: failed unregistering metric: " + metric, ex );
                }
    		}
    	}
    }
    
    public static <M extends AbstractMetric> void addMetric( Map<ObjectName,M> mets, M metric ) { 
        ObjectName objectName = metric.getObjectName();
        LOGGER.debug( "addMetric: metric=" + metric + ", objectName=" + objectName );
        M existing = mets.get( objectName );
        if( existing == null ) {
            LOGGER.debug( "addMetric: adding new MBean " + objectName );
            mets.put( objectName, metric );
        }
        else {
            LOGGER.warn( "addMetric: attempting to re-register the same MBean " + objectName + ", ignoring", new Exception() );
        }
    }
        
    
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

package org.scriptbox.horde.metrics;

import java.lang.management.ManagementFactory

import javax.management.MBeanServer
import javax.management.ObjectName

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.ihg.atp.crs.loadgen.main.LoadScript


public abstract class ScriptMetric extends GeneratorMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger( ScriptMetric.class );
    
    protected LoadScript script;
    
    public void init( LoadScript script ) {
        this.script = script;
    }

    public abstract String getName();
    
    public ObjectName getObjectName() {
        return new ObjectName("GeneratorMetrics:context=${script.context.name},script=${script.name},name=${name}");
    }
    
    public String toString() { 
        return getClass().getName() + "{ objectName=${objectName} }";
    }
}

package org.scriptbox.horde.metrics;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class TestMetric extends GeneratorMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger( TestMetric.class );
    
    protected LoadScript.Test test;
    
    void init( LoadScript script, LoadScript.Test test ) {
        this.script = script;
        this.test = test;
    }

    public ObjectName getObjectName() {
        return new ObjectName("GeneratorMetrics:context=${script.context.name},script=${script.name},test=${test.name},name=${name}");
    }
    
    public abstract String getName();
    
    public abstract void record( boolean success, long millis );
}

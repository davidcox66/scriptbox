package org.scriptbox.horde.metrics;

import org.scriptbox.horde.action.ActionScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractMetric.class );
    
    protected ActionScript script;
    
    public abstract String getName();
    public abstract String getDescription();
    public abstract float getValue();
    
    public String toString() { 
        return getClass().getName() + "{ name=" + getName() + " }";
    }
}

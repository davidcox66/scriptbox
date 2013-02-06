package org.scriptbox.horde.metrics;

import org.scriptbox.horde.action.ActionScript;
import org.scriptbox.horde.metrics.mbean.Exposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractMetric implements Exposable {

    private static final Logger LOGGER = LoggerFactory.getLogger( AbstractMetric.class );
    
    protected ActionScript script;
    
    public String toString() { 
        return getClass().getName() + "{ name=" + getName() + " }";
    }
}

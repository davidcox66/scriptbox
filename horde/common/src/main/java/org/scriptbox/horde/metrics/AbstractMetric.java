package org.scriptbox.horde.metrics;

import org.scriptbox.horde.metrics.mbean.Exposable;


public abstract class AbstractMetric implements Exposable {

    public String toString() { 
        return getClass().getName() + "{ name=" + getName() + " }";
    }
}

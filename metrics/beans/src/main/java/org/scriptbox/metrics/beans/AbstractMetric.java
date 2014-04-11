package org.scriptbox.metrics.beans;

import org.scriptbox.metrics.beans.mbean.Exposable;


public abstract class AbstractMetric implements Exposable {

    public String toString() { 
        return getClass().getName() + "{ name=" + getName() + " }";
    }
}

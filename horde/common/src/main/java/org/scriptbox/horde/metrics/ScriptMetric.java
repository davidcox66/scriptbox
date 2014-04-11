package org.scriptbox.horde.metrics;

import org.scriptbox.horde.action.ActionScript;
import org.scriptbox.metrics.beans.AbstractMetric;


public abstract class ScriptMetric extends AbstractMetric {

    protected ActionScript script;
    
    public void init( ActionScript script ) {
        this.script = script;
    }
}

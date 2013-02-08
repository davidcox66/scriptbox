package org.scriptbox.horde.metrics;

import java.util.Collection;

import org.scriptbox.horde.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ActionMetric extends AbstractMetric implements Recordable, ActionAware {

    private static final Logger LOGGER = LoggerFactory.getLogger( ActionMetric.class );
    
    protected Action action;
    
    public static void collectAll( Collection<? extends ActionMetric> met, final boolean success, final long millis ) {
    	for( ActionMetric metric : met ) {
            metric.record( success, millis );
        }
    }
    
    public void init( Action action ) {
        this.action = action;
        this.script = action.getActionScript();
    }
}

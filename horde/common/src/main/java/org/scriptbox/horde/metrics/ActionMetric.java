package org.scriptbox.horde.metrics;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.scriptbox.horde.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ActionMetric extends AbstractMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger( ActionMetric.class );
    
    protected Action action;
    
    public static void collectAll( Map<ObjectName,? extends ActionMetric> met, final boolean success, final long millis ) {
    	for( ActionMetric metric : met.values() ) {
            metric.record( success, millis );
        }
    }
    
    public void init( Action action ) {
        this.action = action;
        this.script = action.getActionScript();
    }

    public ObjectName getObjectName() {
	    String str = "ActionMetrics:context=" + script.getBoxScript().getContext().getName() + ",script=" + script.getName() + ",action=" + action.getName() + ",name=" + getName();
    	try {
	        return new ObjectName( str );
    	}
    	catch( MalformedObjectNameException ex ) {
    		throw new RuntimeException( "Bad ObjectName: " + str, ex  );
    	}
    }
    
    public abstract String getName();
    
    public abstract void record( boolean success, long millis );
}

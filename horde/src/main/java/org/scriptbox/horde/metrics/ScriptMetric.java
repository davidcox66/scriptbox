package org.scriptbox.horde.metrics;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.scriptbox.horde.action.ActionScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ScriptMetric extends AbstractMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger( ScriptMetric.class );
    
    public void init( ActionScript script ) {
        this.script = script;
    }

    public abstract String getName();
    
    public ObjectName getObjectName() {
	    String str = "ActionMetrics:context=" + script.getBoxScript().getContext().getName() + ",script=" + script.getName() + ",name=" + getName();
    	try {
	        return new ObjectName( str );
    	}
    	catch( MalformedObjectNameException ex ) {
    		throw new RuntimeException( "Bad ObjectName: " + str, ex  );
    	}
    }
    
    public String toString() { 
        return getClass().getName() + "{ objectName=" + getObjectName() + " }";
    }
}

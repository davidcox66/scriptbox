package org.scriptbox.horde.metrics.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.scriptbox.horde.action.ActionScript;

public class ActionScriptDynamicMetricMBean extends AbstractDynamicExposableMBean {

	private ActionScript script;
	
	public ActionScriptDynamicMetricMBean( ActionScript script ) {
		this.script = script;
	}
	
    public ObjectName getObjectName() {
	    String str = "ActionMetrics:context=" + script.getBoxScript().getContext().getName() + ",script=" + script.getName();
    	try {
	        return new ObjectName( str );
    	}
    	catch( MalformedObjectNameException ex ) {
    		throw new RuntimeException( "Bad ObjectName: " + str, ex  );
    	}
    }

}

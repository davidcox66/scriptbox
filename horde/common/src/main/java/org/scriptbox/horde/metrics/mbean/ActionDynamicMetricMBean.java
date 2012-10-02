package org.scriptbox.horde.metrics.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.scriptbox.horde.action.Action;
import org.scriptbox.horde.action.ActionScript;

public class ActionDynamicMetricMBean extends AbstractDynamicMetricMBean {

	private Action action;
	
	public ActionDynamicMetricMBean( Action action ) {
		this.action = action;
	}
	
    public ObjectName getObjectName() {
    	ActionScript script = action.getActionScript();
	    String str = "ActionMetrics:context=" + script.getBoxScript().getContext().getName() + ",script=" + script.getName() + ",action=" + action.getName();
    	try {
	        return new ObjectName( str );
    	}
    	catch( MalformedObjectNameException ex ) {
    		throw new RuntimeException( "Bad ObjectName: " + str, ex  );
    	}
    }
    
}

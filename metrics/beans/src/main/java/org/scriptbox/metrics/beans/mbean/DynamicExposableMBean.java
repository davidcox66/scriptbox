package org.scriptbox.metrics.beans.mbean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class DynamicExposableMBean extends AbstractDynamicExposableMBean {

	private String objectName;
	private String qualifier;
	
	public DynamicExposableMBean( String objectName ) {
		this.objectName = objectName;
	}
	
	public DynamicExposableMBean( String objectName, String qualifier ) {
		this.objectName = objectName;
		this.qualifier = qualifier;
	}
	
    public ObjectName getObjectName() {
	    String str = objectName;
	    if( qualifier != null ) {
	    	str += "," + qualifier;
	    }
    	try {
	        return new ObjectName( str );
    	}
    	catch( MalformedObjectNameException ex ) {
    		throw new RuntimeException( "Bad ObjectName: " + str, ex  );
    	}
    }
    
}

package org.scriptbox.metrics.beans.distro;

import org.scriptbox.metrics.beans.RecordableMetric;


public abstract class AbstractDistroMetric extends RecordableMetric {

    private String name;
    private String fullName;
    protected long low;
    protected long high;
    
    public AbstractDistroMetric( String name, long low, long high ) {
    	this.name = name;
    	if( high > 0 ) {
	        this.fullName = String.format( "%s_%05d_%05d", name, low, high );
    	}
    	else {
	        this.fullName = String.format( "%s_%05d_XXXXX", name, low );
    	}
        this.low = low;
        this.high = high;
    }

    public boolean isInRange( long millis ) {
    	return  millis >= low && (high < 0 || millis <= high); 
    }
    
    public String getName() {
        return fullName;
    } 
    
    public String getDescription() {
    	return name + " distribution " + low + "-" + (high > 0 ? ""+high : "beyond" );
    }
}

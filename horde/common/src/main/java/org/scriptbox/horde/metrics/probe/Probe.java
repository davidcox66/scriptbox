package org.scriptbox.horde.metrics.probe;

import java.util.List;

import org.scriptbox.metrics.beans.mbean.Exposable;

public abstract class Probe {
	
	private String name;
	private String description;

    public Probe( String name, String description ) {
    	this.name = name;
    	this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract List<Exposable> getExposables();
}

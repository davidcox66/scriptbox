package org.scriptbox.horde.metrics.mbean;


public interface Exposable {

	public String getName();
    public String getDescription();
    public float getValue();
}

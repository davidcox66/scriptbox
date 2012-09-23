package org.scriptbox.metrics.model;

import java.util.Map;

public interface MetricTreeNode {

	public String getName();
	public String getType();
    public MetricTreeNode getParent();
	public Map<String,? extends MetricTreeNode> getChildren();
	public MetricTreeNode getChild( String name, String type );
	public MetricSequence getMetricSequence(); 
	public boolean isSequenceAvailable();
}

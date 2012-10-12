package org.scriptbox.metrics.model;

import java.util.Map;

import org.scriptbox.metrics.query.main.MetricProvider;

public interface MetricTreeNode extends MetricProvider {

	public String getId();
	public String getName();
	public String getType();
    public MetricTreeNode getParent();
	public Map<String,? extends MetricTreeNode> getChildren();
	public MetricTreeNode getChild( String name );
	public MetricTreeNode getChild( String name, String type );
	public MetricSequence getMetricSequence(); 
	public boolean isSequenceAvailable();
}

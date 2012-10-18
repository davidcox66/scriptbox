package org.scriptbox.metrics.model;

import java.util.Map;

import org.scriptbox.metrics.query.main.MetricProvider;

/**
 * A node in a tree structure which represents all possible metrics which have been collected. Metrics are
 * associated only with the leaf nodes for the tree.
 * 
 * @author david
 *
 */
public interface MetricTreeNode extends MetricProvider {

	/**
	 * An identifier which uniquely identifies the node within the entire system. This would most likely be a 
	 * composite identifer comprised of all the IDs in the path to this node, including the tree identifier 
	 * as well.
	 */
	public String getId();
	
	/** 
	 * A displayable name for this node which only has to be unique with its set of siblings.
	 */
	public String getName();
	
	/**
	 * A user defined type identifier which may be used for user-defined purposes.
	 */
	public String getType();
	
    public MetricTreeNode getParent();
    
    /**
     * Gets a map of children keyed by the name of the node
     */
	public Map<String,? extends MetricTreeNode> getChildren();

	/**
	 * Gets a node by it's name, returning null if it does not exist
	 */
	public MetricTreeNode getChild( String name );
	
	/**
	 * Attempts to find a child node by the supplied name, creating one with the supplied type if it does not exist
	 */
	public MetricTreeNode getChild( String name, String type );
	
	/**
	 * Gets an object representing the sequence of associated metrics for this tree node. It is an error
	 * to call this method on a node where isSequenceAvailable() returns false. This would typically be
	 * the case when calling on a non-leaf node.
	 */
	public MetricSequence getMetricSequence(); 
	
	/**
	 * Returns true if this node has metrics associated with it. Typically this would only return true for
	 * leaf nodes.
	 */
	public boolean isSequenceAvailable();
}

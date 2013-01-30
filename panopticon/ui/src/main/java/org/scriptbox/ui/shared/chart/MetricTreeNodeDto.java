package org.scriptbox.ui.shared.chart;

import java.io.Serializable;

public class MetricTreeNodeDto implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private MetricTreeParentNodeDto parent;
	private String treeName;
	private String id;
	private String name;
	
	public MetricTreeNodeDto() {
	}
	
	public MetricTreeNodeDto( MetricTreeParentNodeDto parent, String treeName, String id, String name ) {
		this.parent = parent;
		this.treeName = treeName;
		this.id = id;
		this.name = name;
	}

	public MetricTreeParentNodeDto getParent() {
		return parent;
	}
	
	public String getTreeName() {
		return treeName;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

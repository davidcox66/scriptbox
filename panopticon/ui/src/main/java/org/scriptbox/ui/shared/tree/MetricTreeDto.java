package org.scriptbox.ui.shared.tree;

import java.io.Serializable;

public class MetricTreeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String treeName;
	
	public MetricTreeDto() {
	}
	
	public MetricTreeDto( String treeName ) {
		this.treeName = treeName;
	}

	public String getTreeName() {
		return treeName;
	}
	
}

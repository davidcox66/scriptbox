package org.scriptbox.ui.shared.tree;

import java.io.Serializable;
import java.util.List;

public class MetricTreeParentNodeDto extends MetricTreeNodeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<MetricTreeNodeDto> children;
	
	public MetricTreeParentNodeDto() {
	}
	
	public MetricTreeParentNodeDto( String treeName, String id, String name ) {
		super( treeName, id, name );
	}

	public List<MetricTreeNodeDto> getChildren() {
		return children;
	}

	public void setChildren(List<MetricTreeNodeDto> children) {
		this.children = children;
	}

	
}

package org.scriptbox.ui.shared;

import java.io.Serializable;
import java.util.List;

public class TreeParentNodeDto extends TreeNodeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<TreeNodeDto> children;
	
	public TreeParentNodeDto() {
	}
	
	public TreeParentNodeDto( String treeName, String id, String name ) {
		super( treeName, id, name );
	}

	public List<TreeNodeDto> getChildren() {
		return children;
	}

	public void setChildren(List<TreeNodeDto> children) {
		this.children = children;
	}

	
}

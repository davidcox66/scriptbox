package org.scriptbox.ui.shared;

import java.io.Serializable;

public class TreeNodeDto implements Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String treeName;
	private String id;
	private String name;
	
	public TreeNodeDto() {
	}
	
	public TreeNodeDto( String treeName, String id, String name ) {
		this.treeName = treeName;
		this.id = id;
		this.name = name;
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

package org.scriptbox.ui.shared;

import java.io.Serializable;

public class TreeDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String treeName;
	
	public TreeDto() {
	}
	
	public TreeDto( String treeName ) {
		this.treeName = treeName;
	}

	public String getTreeName() {
		return treeName;
	}
	
}

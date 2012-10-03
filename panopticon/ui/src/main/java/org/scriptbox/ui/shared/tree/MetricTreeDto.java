package org.scriptbox.ui.shared.tree;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MetricTreeDto implements Serializable, IsSerializable {

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

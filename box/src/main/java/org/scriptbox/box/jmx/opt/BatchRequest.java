package org.scriptbox.box.jmx.opt;

import java.util.ArrayList;
import java.util.List;

public class BatchRequest {
	
	private List<BatchRequestElement> elements = new ArrayList<BatchRequestElement>();
	
	public void add( BatchRequestElement element ) {
		elements.add( element );
	}

	public List<BatchRequestElement> getElements() {
		return elements;
	}

	public void setElements(List<BatchRequestElement> elements) {
		this.elements = elements;
	}
	
}

package org.scriptbox.box.jmx.opt;

import java.util.ArrayList;
import java.util.List;

public class BatchResponse {

	private List<BatchResponseElement> elements = new ArrayList<BatchResponseElement>();
	
	public void add( BatchResponseElement element ) {
		elements.add( element );
	}

	public List<BatchResponseElement> getElements() {
		return elements;
	}

	public void setElements(List<BatchResponseElement> elements) {
		this.elements = elements;
	}
}

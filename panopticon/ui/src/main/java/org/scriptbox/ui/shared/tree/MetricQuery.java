package org.scriptbox.ui.shared.tree;

import java.util.Date;

public class MetricQuery {

	private MetricTreeNodeDto node;
	private Date start;
	private Date end;
	public MetricTreeNodeDto getNode() {
		return node;
	}
	public void setNode(MetricTreeNodeDto node) {
		this.node = node;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	
	
}

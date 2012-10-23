package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.Date;

import org.scriptbox.ui.shared.timed.TimeBasedLoadConfigBean;


public class MetricQueryDto extends TimeBasedLoadConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private MetricTreeNodeDto node;
	private int resolution;
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
	public int getResolution() {
		return resolution;
	}
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	
}

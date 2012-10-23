package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.Date;

import org.scriptbox.ui.shared.timed.TimeBasedLoadConfigBean;


public class ReportQueryDto extends TimeBasedLoadConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String treeName;
	private String reportName;
	private int resolution;
	private Date start;
	private Date end;
	
	public String getTreeName() {
		return treeName;
	}
	public void setTreeName(String treeName) {
		this.treeName = treeName;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
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

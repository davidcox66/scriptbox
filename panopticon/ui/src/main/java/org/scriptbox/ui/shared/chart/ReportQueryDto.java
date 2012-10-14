package org.scriptbox.ui.shared.chart;

import java.io.Serializable;

import org.scriptbox.ui.shared.timed.TimeBasedLoadConfigBean;


public class ReportQueryDto extends TimeBasedLoadConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String treeName;
	private String reportName;
	
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
	
	
}

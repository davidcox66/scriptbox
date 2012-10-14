package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.ArrayList;

public class MetricReportSummaryDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private ArrayList<String> charts = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<String> getCharts() {
		return charts;
	}
	public void setCharts(ArrayList<String> charts) {
		this.charts = charts;
	}
	
	
}

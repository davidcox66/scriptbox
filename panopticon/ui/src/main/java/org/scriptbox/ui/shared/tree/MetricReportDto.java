package org.scriptbox.ui.shared.tree;

import java.io.Serializable;
import java.util.ArrayList;

public class MetricReportDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<MetricChartDto> charts = new ArrayList<MetricChartDto>();

	public void addChart( MetricChartDto chart ) {
		charts.add( chart );
	}
	
	public ArrayList<MetricChartDto> getCharts() {
		return charts;
	}
}

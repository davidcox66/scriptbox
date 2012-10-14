package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scriptbox.ui.shared.timed.TimeBasedLoadResult;

public class MetricReportDto implements TimeBasedLoadResult<MultiMetricRangeDto>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Date first;
	private Date last;
	private Date start;
	private Date end;
	private ArrayList<MultiMetricRangeDto> charts = new ArrayList<MultiMetricRangeDto>();

	public void addChart( MultiMetricRangeDto chart ) {
		charts.add( chart );
	}
	
	public ArrayList<MultiMetricRangeDto> getCharts() {
		return charts;
	}
	
	public void setCharts(ArrayList<MultiMetricRangeDto> charts) {
		this.charts = charts;
	}
	
	public Date getFirst() {
		return first;
	}

	public void setFirst(Date first) {
		this.first = first;
	}

	public Date getLast() {
		return last;
	}

	public void setLast(Date last) {
		this.last = last;
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

	public List<MultiMetricRangeDto> getData() {
		return charts;
	}
}

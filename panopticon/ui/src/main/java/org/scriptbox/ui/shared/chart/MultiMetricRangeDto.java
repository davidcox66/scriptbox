package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;

public class MultiMetricRangeDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private List<MetricDescriptionDto> lines;
	private List<MultiMetric> metrics;
	
	public MultiMetricRangeDto() {
	}

	
	public MultiMetricRangeDto( String title, List<MetricDescriptionDto> lines, List<MultiMetric> metrics ) {
		this.title = title;
		this.lines = lines;
		this.metrics = metrics;
	}

	public String getTitle() {
		return title;
	}
	
	public List<MultiMetric> getData() {
		return metrics;
	}
	
	public List<MetricDescriptionDto> getLines() {
		return lines;
	}

}

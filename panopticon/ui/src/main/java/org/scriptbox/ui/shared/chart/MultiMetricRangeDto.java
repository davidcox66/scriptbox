package org.scriptbox.ui.shared.chart;

import java.io.Serializable;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;

public class MultiMetricRangeDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;
	private List<MetricDescriptionDto> lines;
	private List<MultiMetric> metrics;
	private String presentation;
	
	public MultiMetricRangeDto() {
	}

	
	public MultiMetricRangeDto( String title, List<MetricDescriptionDto> lines, List<MultiMetric> metrics, String presentation ) {
		this.title = title;
		this.lines = lines;
		this.metrics = metrics;
		this.presentation = presentation;
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


	public String getPresentation() {
		return presentation;
	}

}

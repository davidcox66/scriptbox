package org.scriptbox.ui.server.chart;

import java.util.List;

import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;

public interface MetricDescriptionPostProcessor {

	public void process( ReportElement element, List<MetricDescriptionDto> descriptions );
}

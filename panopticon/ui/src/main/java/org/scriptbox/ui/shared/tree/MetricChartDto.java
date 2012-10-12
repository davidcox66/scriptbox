package org.scriptbox.ui.shared.tree;

import java.io.Serializable;
import java.util.ArrayList;

public class MetricChartDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<MetricRangeDto> series = new ArrayList<MetricRangeDto>();
	
	public ArrayList<MetricRangeDto> getSeries() {
		return series;
	}
	public void addSeries( MetricRangeDto range ) {
		series.add( range );
	}
}

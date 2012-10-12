package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.ui.shared.tree.MultiMetricRangeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.data.shared.ListStore;

public class MultiMetricChartHolder {

	private Chart<MultiMetric> chart;
	private NumericAxis<MultiMetric> valueAxis;
	private TimeAxis<MultiMetric> timeAxis;
	private List<LineSeries<MultiMetric>> series;
	private ListStore<MultiMetric> store;
	private MultiMetricRangeDto chartDto;
	
	public MultiMetricChartHolder(MultiMetricRangeDto chartDto, Date start, Date end) {
		this.chartDto = chartDto;
		valueAxis = MultiMetricChartBuilder.buildValueAxis(chartDto.getLines().size());
		timeAxis = MultiMetricChartBuilder.buildTimeAxis();
		timeAxis.setStartDate(start);
		timeAxis.setEndDate(end);

		store = MultiMetricChartBuilder.buildListStore();
		store.replaceAll(chartDto.getData());
		
		chart = new Chart<MultiMetric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);

		series = new ArrayList<LineSeries<MultiMetric>>();
		int i=0;
		for( String line : chartDto.getLines() ) {
			LineSeries<MultiMetric> ls = MultiMetricChartBuilder.buildValueSeries(line, i++, new RGB(255, 0, 0));
			series.add(ls);
			chart.addSeries(ls);
		}
	}

	public String getTitle() {
		return chartDto.getTitle();
	}
	
	public Chart<MultiMetric> getChart() {
		return chart;
	}

	public NumericAxis<MultiMetric> getValueAxis() {
		return valueAxis;
	}

	public TimeAxis<MultiMetric> getTimeAxis() {
		return timeAxis;
	}

	public List<LineSeries<MultiMetric>> getSeries() {
		return series;
	}

	public ListStore<MultiMetric> getStore() {
		return store;
	}
	
	
}

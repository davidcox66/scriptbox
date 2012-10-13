package org.scriptbox.ui.client.chart.model;

import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.client.chart.builder.MetricChartBuilder;
import org.scriptbox.ui.shared.tree.MetricRangeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.data.shared.ListStore;

public class LineChart {

	private static final Logger logger = Logger.getLogger("LineChart");
	
	// private Timer update;

	private Chart<Metric> chart;
	private NumericAxis<Metric> valueAxis;
	private TimeAxis<Metric> timeAxis;
	private	LineSeries<Metric> series;
	private ListStore<Metric> store;
	
	public LineChart( MetricRangeDto dto ) {
		store = MetricChartBuilder.buildListStore();
		store.replaceAll(dto.getData());
		
		timeAxis = MetricChartBuilder.buildTimeAxis();
		valueAxis = MetricChartBuilder.buildValueAxis();
		series = MetricChartBuilder.buildValueSeries( new RGB(255,0,0) );
		
	    timeAxis.setStartDate(dto.getStart());
        timeAxis.setEndDate(dto.getEnd());
        
        MetricChartBuilder.fixEmptySeries(valueAxis, dto.getData() );
        
		chart = new Chart<Metric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);
		chart.addSeries(series);
	}

	public Chart<Metric> getChart() {
		return chart;
	}

	public NumericAxis<Metric> getValueAxis() {
		return valueAxis;
	}

	public TimeAxis<Metric> getTimeAxis() {
		return timeAxis;
	}

	public LineSeries<Metric> getSeries() {
		return series;
	}

	public ListStore<Metric> getStore() {
		return store;
	}
}

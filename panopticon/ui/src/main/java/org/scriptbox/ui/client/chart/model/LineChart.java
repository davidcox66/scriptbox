package org.scriptbox.ui.client.chart.model;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.client.chart.builder.LineChartBuilder;
import org.scriptbox.ui.shared.chart.MetricRangeDto;

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
		store = LineChartBuilder.buildListStore();
		store.replaceAll(dto.getData());
		
		timeAxis = LineChartBuilder.buildTimeAxis();
		valueAxis = LineChartBuilder.buildValueAxis();
		series = LineChartBuilder.buildValueSeries( new RGB(255,0,0) );
		
	    timeAxis.setStartDate(dto.getStart());
        timeAxis.setEndDate(dto.getEnd());
        
        LineChartBuilder.fixEmptySeries(valueAxis, dto.getData() );
        
		chart = new Chart<Metric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);
		chart.addSeries(series);
	}

	public void setData( MetricRangeDto dto) {
	    timeAxis.setStartDate(dto.getStart());
        timeAxis.setEndDate(dto.getEnd());
        store.replaceAll(dto.getData());
	}
	
	public void addData( MetricRangeDto dto) {
		Date last = timeAxis.getEndDate();
		long millis = last != null ? last.getTime() : Long.MIN_VALUE;
		
        timeAxis.setEndDate(dto.getEnd());
        List<Metric> metrics = dto.getData();
        for( Metric metric : metrics ) {
        	if( metric.getMillis() > millis ) {
        		store.add( metric );
        	}
        }
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

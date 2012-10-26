package org.scriptbox.ui.client.chart.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.ui.client.chart.builder.ChartBuilder;
import org.scriptbox.ui.client.chart.builder.MultiLineChartBuilder;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.scriptbox.ui.shared.chart.MultiMetricRangeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Legend;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.Series;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.data.shared.ListStore;

public class MultiLineChart {


	private Chart<MultiMetric> chart;
	private NumericAxis<MultiMetric> valueAxis;
	private TimeAxis<MultiMetric> timeAxis;
	private List<Series<MultiMetric>> series;
	private ListStore<MultiMetric> store;
	private Legend<MultiMetric> legend;
	private String title;
	
	public MultiLineChart(MultiMetricRangeDto dto, Date start, Date end) {
		title = dto.getTitle();
		List<String> lines = new ArrayList<String>( dto.getLines().size() );
		for( MetricDescriptionDto desc : dto.getLines() ) {
			lines.add( desc.getShortText());
		}
		valueAxis = MultiLineChartBuilder.buildValueAxis(lines);
		timeAxis = MultiLineChartBuilder.buildTimeAxis();
		timeAxis.setStartDate(start);
		timeAxis.setEndDate(end);

	    
		store = MultiLineChartBuilder.buildListStore();
		store.replaceAll(dto.getData());
		
		chart = new Chart<MultiMetric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);
		
		if( lines.size() > 1 ) {
			legend = ChartBuilder.buildLegend( MultiMetric.class );
		    chart.setLegend(legend);
		}

		boolean stacked = "stacked".equals(dto.getPresentation());
		series = stacked ? 
			MultiLineChartBuilder.buildValueAreaSeries(lines) :
			MultiLineChartBuilder.buildValueLineSeries(lines);
			
		for( Series<MultiMetric> ser : series )	 {
			chart.addSeries(ser);
		}
	}

	public void setData( MultiMetricRangeDto dto, Date start, Date end ) {
		timeAxis.setStartDate(start);
		timeAxis.setEndDate( end );
		store.replaceAll(dto.getData());
	}
	
	public void addData( MultiMetricRangeDto dto, Date end ) {
		Date last = timeAxis.getEndDate();
		long millis = last != null ? last.getTime() : Long.MIN_VALUE;
		
        timeAxis.setEndDate(end);
        List<MultiMetric> metrics = dto.getData();
        for( MultiMetric metric : metrics ) {
        	if( metric.getMillis() > millis ) {
        		store.add( metric );
        	}
        }
	}
	
	public String getTitle() {
		return title;
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

	public List<Series<MultiMetric>> getSeries() {
		return series;
	}

	public ListStore<MultiMetric> getStore() {
		return store;
	}

	public Legend<MultiMetric> getLegend() {
		return legend;
	}
	
}

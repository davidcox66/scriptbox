package org.scriptbox.ui.client.chart.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.ui.client.chart.builder.ChartBuilder;
import org.scriptbox.ui.client.chart.builder.MultiLineChartBuilder;
import org.scriptbox.ui.shared.tree.MultiMetricRangeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Legend;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.data.shared.ListStore;

public class MultiLineChart {

	private static RGB[] COLORS = {
		new RGB( 0xFF,0x00, 0x00 ),
		new RGB( 0xFF,0x80, 0x00 ),
		new RGB( 0xFF,0xFF, 0x00 ),
		new RGB( 0x80,0xFF, 0x00 ),
		new RGB( 0x00,0xFF, 0x00 ),
		new RGB( 0x00,0xFF, 0x80 ),
		new RGB( 0x00,0xFF, 0xFF ),
		new RGB( 0x00,0x80, 0xFF ),
		new RGB( 0x00,0x00, 0xFF ),
		new RGB( 0x80,0x00, 0xFF ),
		new RGB( 0xFF,0x00, 0xFF ),
		new RGB( 0xFF,0x00, 0x80 ),
	};
	
	private Chart<MultiMetric> chart;
	private NumericAxis<MultiMetric> valueAxis;
	private TimeAxis<MultiMetric> timeAxis;
	private List<LineSeries<MultiMetric>> series;
	private ListStore<MultiMetric> store;
	private Legend<MultiMetric> legend;
	private String title;
	
	public MultiLineChart(MultiMetricRangeDto dto, Date start, Date end) {
		title = dto.getTitle();
		valueAxis = MultiLineChartBuilder.buildValueAxis(dto.getLines());
		timeAxis = MultiLineChartBuilder.buildTimeAxis();
		timeAxis.setStartDate(start);
		timeAxis.setEndDate(end);

		legend = ChartBuilder.buildLegend( MultiMetric.class );
	    
		store = MultiLineChartBuilder.buildListStore();
		store.replaceAll(dto.getData());
		
		chart = new Chart<MultiMetric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);
	    chart.setLegend(legend);

		series = new ArrayList<LineSeries<MultiMetric>>();
		int i=0;
		for( String line : dto.getLines() ) {
			LineSeries<MultiMetric> ls = MultiLineChartBuilder.buildValueSeries(line, i, COLORS[i % COLORS.length]);
			series.add(ls);
			chart.addSeries(ls);
			i++;
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

	public List<LineSeries<MultiMetric>> getSeries() {
		return series;
	}

	public ListStore<MultiMetric> getStore() {
		return store;
	}

	public Legend<MultiMetric> getLegend() {
		return legend;
	}
	
}

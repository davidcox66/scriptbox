package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;
import org.scriptbox.ui.shared.tree.MetricChartDto;
import org.scriptbox.ui.shared.tree.MetricRangeDto;
import org.scriptbox.ui.shared.tree.MetricReportDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.ReportQueryDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class ReportController {

	private static final Logger logger = Logger.getLogger("ReportController");
	
	// private Timer update;

	static class ReportLine
	{
		LineSeries<Metric> series;
		ListStore<Metric> store;
		
		public ReportLine( MetricRangeDto rangeDto ) {
			series = ChartBuilder
	        ChartBuilder.fixEmptySeries(valueAxis, loaded.getData() );
			
		}
	}
	
	static class ChartHolder
	{
		Chart<Metric> chart;
		NumericAxis<Metric> valueAxis;
		TimeAxis<Metric> timeAxis;
		ArrayList<ReportLine>  lines;
		
		public ChartHolder( MetricChartDto chartDto, Date start, Date end ) {
			valueAxis = ChartBuilder.buildValueAxis();
			timeAxis = ChartBuilder.buildTimeAxis();
			timeAxis.setStartDate(start);
		    timeAxis.setEndDate(end);
		    lines = new ArrayList<ReportLine>( chartDto.getSeries().size() );
		    for( MetricRangeDto range : chartDto.getSeries() ) {
		    	lines.add( new ReportLine(range) );
		    }
		}
	}
	
	private MetricTreeGWTInterfaceAsync service; 
	private ArrayList<ChartHolder> holders;
	private RpcProxy<ReportQueryDto, MetricReportDto> proxy;
	
	public ReportController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<ReportQueryDto, MetricReportDto>() {
			@Override
			public void load(ReportQueryDto loadConfig, AsyncCallback<MetricReportDto> callback) {
				ReportController.this.service.getReport(loadConfig, callback);
			}
		};
		store = ChartBuilder.buildListStore();

		buildValueAxis();
		buildTimeAxis();
		buildValueSeries();
	}

	public Chart<Metric> getChart() {
		return chart;
	}
	
	public void load( String treeName, String reportName, final Runnable callback ) {
		ReportQueryDto query = new ReportQueryDto();
		query.setTreeName( treeName );
		query.setReportName( reportName );
		
		TimeBasedLoader<ReportQueryDto, MetricReportDto> loader= new TimeBasedLoader<ReportQueryDto, MetricReportDto>(proxy);
		loader.addLoadHandler(new LoadHandler<ReportQueryDto, MetricReportDto>() {
			public void onLoad(LoadEvent<ReportQueryDto, MetricReportDto> event) {
				MetricReportDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				
				List<MetricChartDto> charts = loaded.getData();
				holders = new ArrayList<ChartHolder>( charts.size() );
				for( MetricChartDto chart : charts ) {
					holders.add( new ChartHolder(chart,loaded.getStart(),loaded.getEnd()) );
				}
		        
				// setSeriesNameForNode( event.getLoadConfig().getNode() );
		        ChartBuilder.fixEmptySeries(valueAxis, loaded.getData() );
			    store.replaceAll( loaded.getData() );
			    chart = buildChart();
			    callback.run();
				chart.redrawChart();
			}
		} );
		loader.load( query );
		
	}

	private Chart<Metric> buildChart() {
		chart = new Chart<Metric>();
		chart.setStore(store);
		chart.addAxis(timeAxis);
		chart.addAxis(valueAxis);
		chart.addSeries(series);
		return chart;
	}
	
	private void buildTimeAxis() {
		timeAxis = ChartBuilder.buildTimeAxis();
	}
	private void buildValueAxis() {
		valueAxis = ChartBuilder.buildValueAxis();
	}

	private void buildValueSeries() {
		series = ChartBuilder.buildValueSeries( new RGB(255,0,0) );
	}

	private void setSeriesNameForNode( MetricTreeNodeDto node ) { 
		TextSprite title = new TextSprite( node.getName() );
		title.setFontSize(18);
		valueAxis.setTitleConfig(title);
	}
	
}

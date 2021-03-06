package org.scriptbox.ui.client.chart.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.ui.client.PanopticonUI;
import org.scriptbox.ui.client.chart.model.MultiLineChart;
import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricReportDto;
import org.scriptbox.ui.shared.chart.MultiMetricRangeDto;
import org.scriptbox.ui.shared.chart.ReportQueryDto;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent.LoadExceptionHandler;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class ReportChartController extends ChartController {

	private static final Logger logger = Logger.getLogger("ReportChartController");
	
	private ArrayList<MultiLineChart> charts;
	private RpcProxy<ReportQueryDto, MetricReportDto> proxy;
	private String treeName;
	private String reportName;
	
	public ReportChartController( ChartGWTServiceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<ReportQueryDto, MetricReportDto>() {
			@Override
			public void load(ReportQueryDto loadConfig, AsyncCallback<MetricReportDto> callback) {
				ReportChartController.this.service.getReport(loadConfig, callback);
			}
		};
	}

	public void load( final String treeName, final String reportName, Date start, Date end, final int resolution, final Runnable callback ) {
		this.treeName = treeName;
		this.reportName = reportName;
		this.start = start;
		this.end = end;
		this.resolution = resolution;
		
		ReportQueryDto query = new ReportQueryDto();
		query.setTreeName( treeName );
		query.setReportName( reportName );
		query.setResolution( resolution );
		
		TimeBasedLoader<ReportQueryDto, MetricReportDto> loader= new TimeBasedLoader<ReportQueryDto, MetricReportDto>(proxy);
		loader.addLoadHandler(new LoadHandler<ReportQueryDto, MetricReportDto>() {
			public void onLoad(LoadEvent<ReportQueryDto, MetricReportDto> event) {
				MetricReportDto dto = event.getLoadResult();
				last = dto.getLast();
				List<MultiMetricRangeDto> ranges = dto.getData();
				logger.log( Level.INFO, "onLoad: start=" + dto.getStart() + ", end=" + dto.getEnd() + 
					", charts.size()=" + ranges.size() ); 
				charts = new ArrayList<MultiLineChart>( ranges.size() );
				for( MultiMetricRangeDto range : ranges ) {
					MultiLineChart chart = new MultiLineChart(range,dto.getStart(),dto.getEnd() );
					charts.add( chart );
				}
		        if( callback != null ) { 
					callback.run();
		        }
				for( MultiLineChart chart : charts ) {
					chart.getChart().redrawChart();
				}
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}

	public void update() {
		ReportQueryDto query = new ReportQueryDto();
		query.setTreeName( treeName );
		query.setReportName( reportName );
		query.setStart( getUpdateDate() );
		query.setEnd( null );
		query.setResolution( resolution );

		final Map<String,MultiLineChart> chartsByTitle = getTitleChartMap();
		
		TimeBasedLoader<ReportQueryDto, MetricReportDto> loader= new TimeBasedLoader<ReportQueryDto, MetricReportDto>(proxy);
		loader.addLoadHandler(new LoadHandler<ReportQueryDto, MetricReportDto>() {
			public void onLoad(LoadEvent<ReportQueryDto, MetricReportDto> event) {
				MetricReportDto dto = event.getLoadResult();
				last = dto.getLast();
				List<MultiMetricRangeDto> ranges = dto.getData();
				logger.log( Level.INFO, "onLoad: start=" + dto.getStart() + ", end=" + dto.getEnd() + 
					", charts.size()=" + ranges.size() ); 
				
				for( MultiMetricRangeDto range : ranges ) {
					MultiLineChart chart = chartsByTitle.get( range.getTitle() );
					if( chart != null ) {
						chart.setData( range, dto.getStart(),dto.getEnd() );
					}
					else {
						logger.log( Level.WARNING, "reload: existing chart not found: " + range.getTitle() );
					}
				}
				for( MultiLineChart chart : charts ) {
					chart.getChart().redrawChart();
				}
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}
	public void reload() {
		reload( start, end, resolution );
	}
	
	public void reload( Date start, Date end, int resolution ) {
		ReportQueryDto query = new ReportQueryDto();
		query.setTreeName( treeName );
		query.setReportName( reportName );
		query.setStart( start );
		query.setEnd( end );
		query.setResolution( resolution );

		final Map<String,MultiLineChart> chartsByTitle = getTitleChartMap();
		
		TimeBasedLoader<ReportQueryDto, MetricReportDto> loader= new TimeBasedLoader<ReportQueryDto, MetricReportDto>(proxy);
		loader.addLoadHandler(new LoadHandler<ReportQueryDto, MetricReportDto>() {
			public void onLoad(LoadEvent<ReportQueryDto, MetricReportDto> event) {
				MetricReportDto dto = event.getLoadResult();
				last = dto.getLast();
				List<MultiMetricRangeDto> ranges = dto.getData();
				logger.log( Level.INFO, "onLoad: start=" + dto.getStart() + ", end=" + dto.getEnd() + 
					", charts.size()=" + ranges.size() ); 
				
				for( MultiMetricRangeDto range : ranges ) {
					MultiLineChart chart = chartsByTitle.get( range.getTitle() );
					if( chart != null ) {
						chart.setData( range, dto.getStart(),dto.getEnd() );
					}
					else {
						logger.log( Level.WARNING, "reload: existing chart not found: " + range.getTitle() );
					}
				}
				for( MultiLineChart chart : charts ) {
					chart.getChart().redrawChart();
				}
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}

	private Map<String,MultiLineChart> getTitleChartMap() {
		final Map<String,MultiLineChart> chartsByTitle = new HashMap<String,MultiLineChart>();
		for( MultiLineChart chart : charts ) {
			chartsByTitle.put( chart.getTitle(), chart );
		}
		return chartsByTitle;
	}

	private void addErrorHandler( TimeBasedLoader<ReportQueryDto, MetricReportDto> loader ) {
		loader.addLoadExceptionHandler( new LoadExceptionHandler<ReportQueryDto>() {
			public void onLoadException(LoadExceptionEvent<ReportQueryDto> event) {
				PanopticonUI.error( event.getException() );
			}
		} );
	}
	public List<MultiLineChart> getCharts() {
		return charts;
	}
}

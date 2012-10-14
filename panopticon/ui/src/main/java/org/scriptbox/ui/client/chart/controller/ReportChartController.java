package org.scriptbox.ui.client.chart.controller;

import java.util.ArrayList;
import java.util.List;
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
import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
import com.sencha.gxt.widget.core.client.info.Info;

public class ReportChartController {

	private static final Logger logger = Logger.getLogger("ReportChartController");
	
	private ChartGWTServiceAsync service; 
	private ArrayList<MultiLineChart> charts;
	private RpcProxy<ReportQueryDto, MetricReportDto> proxy;
	
	public ReportChartController( ChartGWTServiceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<ReportQueryDto, MetricReportDto>() {
			@Override
			public void load(ReportQueryDto loadConfig, AsyncCallback<MetricReportDto> callback) {
				ReportChartController.this.service.getReport(loadConfig, callback);
			}
		};
	}

	public void load( final String treeName, final String reportName, final Runnable callback ) {
		ReportQueryDto query = new ReportQueryDto();
		query.setTreeName( treeName );
		query.setReportName( reportName );
		
		TimeBasedLoader<ReportQueryDto, MetricReportDto> loader= new TimeBasedLoader<ReportQueryDto, MetricReportDto>(proxy);
		loader.addLoadHandler(new LoadHandler<ReportQueryDto, MetricReportDto>() {
			public void onLoad(LoadEvent<ReportQueryDto, MetricReportDto> event) {
				MetricReportDto dto = event.getLoadResult();
				List<MultiMetricRangeDto> ranges = dto.getData();
				logger.log( Level.INFO, "onLoad: start=" + dto.getStart() + ", end=" + dto.getEnd() + 
					", charts.size()=" + ranges.size() ); 
				
				charts = new ArrayList<MultiLineChart>( ranges.size() );
				for( MultiMetricRangeDto range : ranges ) {
					MultiLineChart chart = new MultiLineChart(range,dto.getStart(),dto.getEnd() );
					charts.add( chart );
				}
		        
				callback.run();
				
				for( MultiLineChart chart : charts ) {
					chart.getChart().redrawChart();
				}
			}
		} );
		loader.addLoadExceptionHandler( new LoadExceptionHandler<ReportQueryDto>() {
			public void onLoadException(LoadExceptionEvent<ReportQueryDto> event) {
				PanopticonUI.error( event.getException() );
			}
		} );
		loader.load( query );
	}

	public List<MultiLineChart> getCharts() {
		return charts;
	}
}

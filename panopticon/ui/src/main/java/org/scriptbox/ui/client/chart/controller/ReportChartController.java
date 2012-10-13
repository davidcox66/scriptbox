package org.scriptbox.ui.client.chart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.ui.client.chart.model.MultiLineChart;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;
import org.scriptbox.ui.shared.tree.MetricReportDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MultiMetricRangeDto;
import org.scriptbox.ui.shared.tree.ReportQueryDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class ReportChartController {

	private static final Logger logger = Logger.getLogger("ReportChartController");
	
	private MetricTreeGWTInterfaceAsync service; 
	private ArrayList<MultiLineChart> charts;
	private RpcProxy<ReportQueryDto, MetricReportDto> proxy;
	
	public ReportChartController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<ReportQueryDto, MetricReportDto>() {
			@Override
			public void load(ReportQueryDto loadConfig, AsyncCallback<MetricReportDto> callback) {
				ReportChartController.this.service.getReport(loadConfig, callback);
			}
		};
	}

	public void load( String treeName, String reportName, final Runnable callback ) {
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
		loader.load( query );
	}

	public List<MultiLineChart> getCharts() {
		return charts;
	}
}

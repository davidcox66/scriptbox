package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.timed.TimeBasedLoader;
import org.scriptbox.ui.shared.tree.MetricReportDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MultiMetricRangeDto;
import org.scriptbox.ui.shared.tree.ReportQueryDto;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class ReportController {

	private static final Logger logger = Logger.getLogger("ReportController");
	
	private MetricTreeGWTInterfaceAsync service; 
	private ArrayList<MultiMetricChartHolder> holders;
	private RpcProxy<ReportQueryDto, MetricReportDto> proxy;
	
	public ReportController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<ReportQueryDto, MetricReportDto>() {
			@Override
			public void load(ReportQueryDto loadConfig, AsyncCallback<MetricReportDto> callback) {
				ReportController.this.service.getReport(loadConfig, callback);
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
				MetricReportDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				
				List<MultiMetricRangeDto> charts = loaded.getData();
				holders = new ArrayList<MultiMetricChartHolder>( charts.size() );
				for( MultiMetricRangeDto chart : charts ) {
					MultiMetricChartHolder holder = new MultiMetricChartHolder(chart,loaded.getStart(),loaded.getEnd() );
					holders.add( holder );
				}
		        
				callback.run();
				
				for( MultiMetricChartHolder holder : holders ) {
					holder.getChart().redrawChart();
				}
			}
		} );
		loader.load( query );
	}

	public List<MultiMetricChartHolder> getCharts() {
		return holders;
	}
}

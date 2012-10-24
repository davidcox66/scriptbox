package org.scriptbox.ui.client.chart.controller;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.client.PanopticonUI;
import org.scriptbox.ui.client.chart.model.LineChart;
import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricQueryDto;
import org.scriptbox.ui.shared.chart.MetricRangeDto;
import org.scriptbox.ui.shared.chart.MetricReportDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;
import org.scriptbox.ui.shared.chart.ReportQueryDto;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;
import com.sencha.gxt.data.shared.loader.LoadExceptionEvent.LoadExceptionHandler;

public class LineChartController extends ChartController {

	private static final Logger logger = Logger.getLogger("LineChartController");
	
	// private Timer update;

	private ChartGWTServiceAsync service; 
	private RpcProxy<MetricQueryDto, MetricRangeDto> proxy;
	private LineChart chart;
	private MetricTreeNodeDto node;
	private Date start;
	private Date end;
	private int resolution;
	private Date last;
	
	public LineChartController( ChartGWTServiceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<MetricQueryDto, MetricRangeDto>() {
			@Override
			public void load(MetricQueryDto loadConfig, AsyncCallback<MetricRangeDto> callback) {
				LineChartController.this.service.getMetrics(loadConfig, callback);
			}
		};
	}

	public LineChart getChart() {
		return chart;
	}
	
	public void load( MetricTreeNodeDto node, Date start, Date end, int resolution, final Runnable callback ) {
		this.node = node;
		this.start = start;
		this.end = end;
		this.resolution = resolution;
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setStart( start );
		query.setEnd( end );
		query.setResolution( resolution );
		
		TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader= new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(new LoadHandler<MetricQueryDto, MetricRangeDto>() {
			public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
				MetricRangeDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				last = loaded.getLast();
				chart = new LineChart( loaded );
				if( callback != null ) {
				    callback.run();
				}
				chart.getChart().redrawChart();
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}
	
	public void update() {
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setStart( last != null ? new Date(last.getTime()+(resolution*1000)) : start );
		query.setEnd( new Date(Long.MAX_VALUE) );
		query.setResolution( resolution );
		
		TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader= new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(new LoadHandler<MetricQueryDto, MetricRangeDto>() {
			public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
				MetricRangeDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				
				chart.addData( loaded );
				chart.getChart().redrawChart();
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}
	
	public void reload() {
		reload( start, end, resolution );
	}
	public void reload( Date start, Date end, int resolution ) {
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setResolution( resolution );
		query.setStart( start );
		query.setEnd( end );
		
		TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader= new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(new LoadHandler<MetricQueryDto, MetricRangeDto>() {
			public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
				MetricRangeDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				last = loaded.getLast();
				
				chart.setData( loaded );
				chart.getChart().redrawChart();
			}
		} );
		addErrorHandler( loader );
		loader.load( query );
	}
	
	private void addErrorHandler( TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader ) {
		loader.addLoadExceptionHandler( new LoadExceptionHandler<MetricQueryDto>() {
			public void onLoadException(LoadExceptionEvent<MetricQueryDto> event) {
				PanopticonUI.error( event.getException() );
			}
		} );
	}

}

package org.scriptbox.ui.client.chart.controller;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.ui.client.chart.model.LineChart;
import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricQueryDto;
import org.scriptbox.ui.shared.chart.MetricRangeDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class LineChartController {

	private static final Logger logger = Logger.getLogger("LineChartController");
	
	// private Timer update;

	private ChartGWTServiceAsync service; 
	private RpcProxy<MetricQueryDto, MetricRangeDto> proxy;
	private LineChart chart;
	private MetricTreeNodeDto node;
	
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
	
	public void load( MetricTreeNodeDto node, final Runnable callback ) {
		this.node = node;
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setStart( new Date() );
		query.setEnd( new Date() );
		
		TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader= new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(new LoadHandler<MetricQueryDto, MetricRangeDto>() {
			public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
				MetricRangeDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				
				chart = new LineChart( loaded );
				if( callback != null ) {
				    callback.run();
				}
				chart.getChart().redrawChart();
			}
		} );
		loader.load( query );
	}
	
	public void reload( final Runnable callback ) {
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setStart( new Date() );
		query.setEnd( new Date() );
		
		TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader= new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(new LoadHandler<MetricQueryDto, MetricRangeDto>() {
			public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
				MetricRangeDto loaded = event.getLoadResult();
				logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
					", values.size()=" + loaded.getData().size() ); 
				
				chart.setData( loaded );
				if( callback != null ) {
				    callback.run();
				}
				chart.getChart().redrawChart();
			}
		} );
		loader.load( query );
		
	}
}

package org.scriptbox.ui.client;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;
import org.scriptbox.ui.shared.tree.MetricQueryDto;
import org.scriptbox.ui.shared.tree.MetricRangeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

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

public class ChartController {

	private static final Logger logger = Logger.getLogger("ChartController");
	
	// private Timer update;

	private MetricTreeGWTInterfaceAsync service; 
	private Chart<Metric> chart;
	private NumericAxis<Metric> valueAxis;
	private TimeAxis<Metric> timeAxis;
	private	LineSeries<Metric> series;
	private RpcProxy<MetricQueryDto, MetricRangeDto> proxy;
	private ListStore<Metric> store;
	
	public ChartController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<MetricQueryDto, MetricRangeDto>() {
			@Override
			public void load(MetricQueryDto loadConfig, AsyncCallback<MetricRangeDto> callback) {
				ChartController.this.service.getMetrics(loadConfig, callback);
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
	
	public void load( MetricTreeNodeDto node, final Runnable callback ) {
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
				
			    timeAxis.setStartDate(loaded.getStart());
		        timeAxis.setEndDate(loaded.getEnd());
		        
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

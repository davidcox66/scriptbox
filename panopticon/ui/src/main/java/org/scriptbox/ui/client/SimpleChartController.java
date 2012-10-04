package org.scriptbox.ui.client;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.timed.TimeBasedLoader;
import org.scriptbox.ui.shared.tree.MetricQueryDto;
import org.scriptbox.ui.shared.tree.MetricRangeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.SeriesLabelProvider;
import com.sencha.gxt.chart.client.chart.series.SeriesToolTipConfig;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class SimpleChartController {

	private static final Logger logger = Logger.getLogger("ChartListPanel");
	
	public interface MetricPropertyAccess extends PropertyAccess<Metric> {
		@Path("millis")
		ModelKeyProvider<Metric> key();
		ValueProvider<Metric, Date> date();
		ValueProvider<Metric, Float> value();
	}

	private static final MetricPropertyAccess metricAccess = GWT.create(MetricPropertyAccess.class);
	private static final DateTimeFormat f = DateTimeFormat.getFormat("HH:mm:ss");

	// private Timer update;

	private MetricTreeGWTInterfaceAsync service; 
	private Chart<Metric> chart;
	private NumericAxis<Metric> valueAxis;
	private TimeAxis<Metric> timeAxis;
	private	LineSeries<Metric> series;
	private RpcProxy<MetricQueryDto, MetricRangeDto> proxy;
	private ListStore<Metric> store;
	
	public SimpleChartController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		proxy = new RpcProxy<MetricQueryDto, MetricRangeDto>() {
			@Override
			public void load(MetricQueryDto loadConfig, AsyncCallback<MetricRangeDto> callback) {
				SimpleChartController.this.service.getMetrics(loadConfig, callback);
			}
		};

		store = new ListStore<Metric>(metricAccess.key());

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
		        float singleValue = getSingleValue( loaded.getData() );
		        if( singleValue != Float.NaN ) {
					valueAxis.setAdjustMaximumByMajorUnit( false );
					valueAxis.setAdjustMinimumByMajorUnit( false );
					valueAxis.setMinimum(singleValue - 2 ); 
					valueAxis.setMaximum(singleValue + 2 ); 
		        }
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
		timeAxis = new TimeAxis<Metric>();
		timeAxis.setField(metricAccess.date());
		timeAxis.setStartDate(new Date());
		timeAxis.setEndDate(new Date());
		// timeAxis.setLabelOverlapHiding( true );
		
		TextSprite textSprite = new TextSprite();
	    textSprite.setRotation(315);
	    timeAxis.setLabelConfig(textSprite);
	    timeAxis.setLabelPadding(-5);
	    // timeAxis.setMinorTickSteps(5);
	    // timeAxis.setLabelTolerance(60);
	    // timeAxis.setLabelStepRatio(2);
	    
		timeAxis.setLabelProvider(new LabelProvider<Date>() {
			@Override
			public String getLabel(Date item) {
				return f.format(item);
			}
		});
	}
	private void buildValueAxis() {
		valueAxis = new NumericAxis<Metric>();
		valueAxis.setPosition(Position.LEFT);
		valueAxis.addField(metricAccess.value());
		valueAxis.setDisplayGrid(true);
		
		valueAxis.setAdjustMaximumByMajorUnit( true );
		valueAxis.setAdjustMinimumByMajorUnit( true );
		// valueAxis.setMinimum(0); 
	}

	private void buildValueSeries() {
		series = new LineSeries<Metric>();
		series.setYAxisPosition(Position.LEFT);
		series.setYField(metricAccess.value());
		series.setStroke(new RGB(255,0,0));
		// series.setStroke(new RGB(148, 174, 10));
		// series.setShowMarkers(true);
		// series.setMarkerIndex(1);
		// Sprite marker = Primitives.circle(0, 0, 6);
		// marker.setFill(new RGB(148, 174, 10));
		// series.setMarkerConfig(marker);
		
		SeriesToolTipConfig<Metric> toolTip = new SeriesToolTipConfig<Metric>();
	    toolTip.setTrackMouse(true);
	    toolTip.setHideDelay(20);
	    toolTip.setLabelProvider(new SeriesLabelProvider<Metric>() {
	      @Override
	      public String getLabel(Metric item, ValueProvider<? super Metric, ? extends Number> valueProvider) {
	        return f.format(item.getDate()) + " - " + item.getValue();
	      }
	    });
	    series.setToolTipConfig(toolTip);
	}

	private void setSeriesNameForNode( MetricTreeNodeDto node ) { 
		TextSprite title = new TextSprite( node.getName() );
		title.setFontSize(18);
		valueAxis.setTitleConfig(title);
	}
	
	/**
	 * Deal with a problem in GXT where the series is all the same value and it can't calculate
	 * a min/max for the axis
	 * 
	 * @param metrics
	 * @return
	 */
	private float getSingleValue( List<Metric> metrics ) {
		if( metrics.size() > 0 ) {
			float val = metrics.get(0).getValue();
			for( Metric metric : metrics ) {
				if( metric.getValue() != val ) {
					return Float.NaN;
				}
			}
			return val;
		}
		return Float.NaN;
	}	
}

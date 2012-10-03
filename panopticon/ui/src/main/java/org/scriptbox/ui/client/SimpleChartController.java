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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.Primitives;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.path.PathSprite;
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

public class SimpleChartController implements LoadHandler<MetricQueryDto, MetricRangeDto> {

	private static final Logger logger = Logger.getLogger("ChartListPanel");
	
	public interface MetricPropertyAccess extends PropertyAccess<Metric> {
		ModelKeyProvider<Metric> nameKey();
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
	private	TimeBasedLoader<MetricQueryDto, MetricRangeDto> loader;
	private ListStore<Metric> store;
	
	
	public SimpleChartController( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;

		RpcProxy<MetricQueryDto, MetricRangeDto> proxy = new RpcProxy<MetricQueryDto, MetricRangeDto>() {
			@Override
			public void load(MetricQueryDto loadConfig, AsyncCallback<MetricRangeDto> callback) {
				SimpleChartController.this.service.getMetrics(loadConfig, callback);
			}
		};

		loader = new TimeBasedLoader<MetricQueryDto, MetricRangeDto>(proxy);
		loader.addLoadHandler(this);
		store = new ListStore<Metric>(metricAccess.nameKey());
		
		chart = new Chart<Metric>();
		chart.setStore(store);

		buildValueAxis();
		buildTimeAxis();
		buildValueSeries();
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
	    timeAxis.setMinorTickSteps(5);
	    // timeAxis.setLabelTolerance(60);
	    timeAxis.setLabelStepRatio(2);
	    
	    PathSprite pathSprite = new PathSprite();
	    timeAxis.setAxisConfig(pathSprite);
	    
		timeAxis.setLabelProvider(new LabelProvider<Date>() {
			@Override
			public String getLabel(Date item) {
				return f.format(item);
			}
		});
		chart.addAxis(timeAxis);
	}
	private void buildValueAxis() {
		valueAxis = new NumericAxis<Metric>();
		valueAxis.setPosition(Position.LEFT);
		valueAxis.setWidth( 40 );
		valueAxis.addField(metricAccess.value());
		valueAxis.setDisplayGrid(true);
		// TextSprite title = new TextSprite("Value");
		// title.setFontSize(18);
		// valueAxis.setTitleConfig(title);
		// valueAxis.setAdjustMaximumByMajorUnit( true );
		// valueAxis.setAdjustMinimumByMajorUnit( true );
		// valueAxis.setMinimum(0);
		// valueAxis.setMaximum(100);
		
		chart.addAxis(valueAxis);
	}

	private void buildValueSeries() {
		series = new LineSeries<Metric>();
		series.setYAxisPosition(Position.LEFT);
		series.setYField(metricAccess.value());
		series.setStroke(new RGB(148, 174, 10));
		series.setShowMarkers(true);
		series.setMarkerIndex(1);
		Sprite marker = Primitives.circle(0, 0, 6);
		marker.setFill(new RGB(148, 174, 10));
		series.setMarkerConfig(marker);
		chart.addSeries(series);
	}
	
	public Chart<Metric> getChart() {
		return chart;
	}
	
	public void load( MetricTreeNodeDto node ) {
		MetricQueryDto query = new MetricQueryDto();
		query.setNode( node );
		query.setStart( new Date() );
		query.setEnd( new Date() );
		loader.load( query );
	}
	
	@Override
	public void onLoad(LoadEvent<MetricQueryDto, MetricRangeDto> event) {
		MetricRangeDto loaded = event.getLoadResult();
        double axisMin = loaded.getMin() > 0 && loaded.getMin() != loaded.getMax() ? 
        	loaded.getMin() - ((loaded.getMax() - loaded.getMin()) * 0.05) : 0;
        double axisMax = loaded.getMax()+1;
        
		logger.log( Level.INFO, "onLoad: start=" + loaded.getStart() + ", end=" + loaded.getEnd() + 
			", values.size()=" + loaded.getData().size() + 
			", min=" + loaded.getMin() + ", max=" + loaded.getMax() +
			", axisMin=" + axisMin + ", axisMax=" + axisMax );
		
	    timeAxis.setStartDate(loaded.getStart());
        timeAxis.setEndDate(loaded.getEnd());
		valueAxis.setMinimum( axisMin );
		valueAxis.setMaximum( axisMax );
	    store.replaceAll( loaded.getData() );
        // chart.redrawChart();
        chart.redrawChartForced();
	}	
}

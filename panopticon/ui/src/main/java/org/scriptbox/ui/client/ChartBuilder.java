package org.scriptbox.ui.client;

import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.Metric;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.SeriesLabelProvider;
import com.sencha.gxt.chart.client.chart.series.SeriesToolTipConfig;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

public class ChartBuilder {

	private static ValueProvider<Metric,Float> valueProvider = new ValueProvider<Metric,Float>() {
		public Float getValue(Metric metric) {
			return metric.getValue();
		}
		public void setValue(Metric object, Float value) {
		}
		public String getPath() {
			return "value";
		}
	};
	
	private static ValueProvider<Metric,Date> dateProvider = new ValueProvider<Metric,Date>() {
		public Date getValue(Metric metric) {
			return metric.getDate();
		}
		public void setValue(Metric object, Date value) {
		}
		public String getPath() {
			return "date";
		}
	};
	
	private static ModelKeyProvider<Metric> keyProvider = new ModelKeyProvider<Metric>() {
		 public String getKey(Metric item) {
			 return String.valueOf(item.getMillis());
		 }
	};
	
	private static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
	
	public static ListStore<Metric> buildListStore() {
		return new ListStore<Metric>(keyProvider);	
	}
	
	public static NumericAxis<Metric> buildValueAxis() {
		NumericAxis<Metric> valueAxis = new NumericAxis<Metric>();
		valueAxis.setPosition(Position.LEFT);
		valueAxis.addField(valueProvider);
		valueAxis.setDisplayGrid(true);
		
		valueAxis.setAdjustMaximumByMajorUnit( true );
		valueAxis.setAdjustMinimumByMajorUnit( true );
		
		valueAxis.setLabelProvider(new LabelProvider<Number>() {
			  public String getLabel(Number item) {
			    return NumberFormat.getFormat("0.00").format(item.doubleValue());
			  }
			}
		);

		// valueAxis.setMinimum(0); 
		return valueAxis;
	}
	public static TimeAxis<Metric> buildTimeAxis() {
		TimeAxis<Metric> timeAxis = new TimeAxis<Metric>();
		timeAxis.setField(dateProvider);
		timeAxis.setStartDate(new Date());
		timeAxis.setEndDate(new Date());
		timeAxis.setLabelOverlapHiding( true );
		
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
				return dateFormat.format(item);
			}
		});
		return timeAxis;
	}

	public static LineSeries<Metric> buildValueSeries( Color color ) {
		LineSeries<Metric> series = new LineSeries<Metric>();
		series.setYAxisPosition(Position.LEFT);
		series.setYField(valueProvider);
		series.setStroke(color);
		
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
	        return dateFormat.format(item.getDate()) + " - " + item.getValue();
	      }
	    });
	    series.setToolTipConfig(toolTip);
	    
	    return series;
	}
	
	public static void fixEmptySeries( NumericAxis<Metric> valueAxis, List<Metric> data ) {
        float singleValue = getSingleValue( data );
        if( singleValue != Float.NaN ) {
			valueAxis.setAdjustMaximumByMajorUnit( false );
			valueAxis.setAdjustMinimumByMajorUnit( false );
			valueAxis.setMinimum(singleValue - 2 ); 
			valueAxis.setMaximum(singleValue + 2 ); 
        }
	}
	
	/**
	 * Deal with a problem in GXT where the series is all the same value and it can't calculate
	 * a min/max for the axis
	 * 
	 * @param metrics
	 * @return
	 */
	private static float getSingleValue( List<Metric> metrics ) {
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

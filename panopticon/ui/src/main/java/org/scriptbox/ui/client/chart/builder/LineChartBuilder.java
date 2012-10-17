package org.scriptbox.ui.client.chart.builder;

import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.Metric;

import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.SeriesLabelProvider;
import com.sencha.gxt.chart.client.chart.series.SeriesToolTipConfig;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

public class LineChartBuilder extends ChartBuilder {

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
	
	public static ListStore<Metric> buildListStore() {
		return new ListStore<Metric>(keyProvider);	
	}
	
	public static NumericAxis<Metric> buildValueAxis() {
		NumericAxis<Metric> valueAxis = buildValueAxis(Metric.class);
		valueAxis.addField(valueProvider);
		return valueAxis;
	}
	
	public static TimeAxis<Metric> buildTimeAxis() {
		TimeAxis<Metric> timeAxis = buildTimeAxis( Metric.class );
		timeAxis.setField(dateProvider);
		return timeAxis;
	}
	
	public static LineSeries<Metric> buildValueSeries( Color color ) {
		LineSeries<Metric> series = buildValueSeries( Metric.class, color );
		series.setYField(valueProvider);
		SeriesToolTipConfig<Metric> toolTip = new SeriesToolTipConfig<Metric>();
	    toolTip.setTrackMouse(true);
	    toolTip.setHideDelay(2);
	    toolTip.setLabelProvider(new SeriesLabelProvider<Metric>() {
	      @Override
	      public String getLabel(Metric item, ValueProvider<? super Metric, ? extends Number> valueProvider) {
	        return dateFormat.format(item.getDate()) + " - " + numberFormat.format(item.getValue());
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

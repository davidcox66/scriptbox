package org.scriptbox.ui.client;

import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MultiMetric;

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

public class MultiMetricChartBuilder extends ChartBuilder {

	private static ValueProvider<MultiMetric,Date> dateProvider = new ValueProvider<MultiMetric,Date>() {
		public Date getValue(MultiMetric metric) {
			return metric.getDate();
		}
		public void setValue(MultiMetric object, Date value) {
		}
		public String getPath() {
			return "date";
		}
	};
	
	private static ModelKeyProvider<MultiMetric> keyProvider = new ModelKeyProvider<MultiMetric>() {
		 public String getKey(MultiMetric item) {
			 return String.valueOf(item.getMillis());
		 }
	};
	
	static class MultiMetricValueProvider implements ValueProvider<MultiMetric,Float> {
		
		private int index;
		
		public MultiMetricValueProvider(int index ) {
			this.index = index;
		}
		
		public Float getValue(MultiMetric metric) {
			return metric.getValues()[index];
		}
		public void setValue(MultiMetric object, Float value) {
		}
		public String getPath() {
			return "value";
		}
	};
	
	public static ListStore<MultiMetric> buildListStore() {
		return new ListStore<MultiMetric>(keyProvider);	
	}
	
	public static NumericAxis<MultiMetric> buildValueAxis( int count ) {
		NumericAxis<MultiMetric> valueAxis = buildValueAxis(MultiMetric.class);
		for( int i=0 ; i < count ; i++ ) {
			valueAxis.addField(new MultiMetricValueProvider(i) );
		}
		return valueAxis;
	}
	
	public static TimeAxis<MultiMetric> buildTimeAxis() {
		TimeAxis<MultiMetric> timeAxis = buildTimeAxis( MultiMetric.class );
		timeAxis.setField(dateProvider);
		return timeAxis;
	}
	
	public static LineSeries<MultiMetric> buildValueSeries( final String label, final int index, Color color ) {
		LineSeries<MultiMetric> series = buildValueSeries( MultiMetric.class, color );
		series.setYField( new MultiMetricValueProvider(index) );
		SeriesToolTipConfig<MultiMetric> toolTip = new SeriesToolTipConfig<MultiMetric>();
	    toolTip.setTrackMouse(true);
	    toolTip.setHideDelay(20);
	    toolTip.setLabelProvider(new SeriesLabelProvider<MultiMetric>() {
	      @Override
	      public String getLabel(MultiMetric item, ValueProvider<? super MultiMetric, ? extends Number> valueProvider) {
	        return dateFormat.format(item.getDate()) + " - " + label + " - " + item.getValues()[index];
	      }
	    });
	    series.setToolTipConfig(toolTip);
	    
		return series;
	}
}

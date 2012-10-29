package org.scriptbox.ui.client.chart.builder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.MultiMetric;

import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.AreaSeries;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.Series;
import com.sencha.gxt.chart.client.chart.series.SeriesLabelProvider;
import com.sencha.gxt.chart.client.chart.series.SeriesToolTipConfig;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;

public class MultiLineChartBuilder extends ChartBuilder {

	private static RGB[] COLORS = {
		new RGB( 0xFF,0x00, 0x00 ),
		new RGB( 0xFF,0x80, 0x00 ),
		new RGB( 0xFF,0xFF, 0x00 ),
		new RGB( 0x80,0xFF, 0x00 ),
		new RGB( 0x00,0xFF, 0x00 ),
		new RGB( 0x00,0xFF, 0x80 ),
		new RGB( 0x00,0xFF, 0xFF ),
		new RGB( 0x00,0x80, 0xFF ),
		new RGB( 0x00,0x00, 0xFF ),
		new RGB( 0x80,0x00, 0xFF ),
		new RGB( 0xFF,0x00, 0xFF ),
		new RGB( 0xFF,0x00, 0x80 ),
	};
	
	
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
		private String path;
		
		public MultiMetricValueProvider( int index ) {
			this( index, "value");
		}
		public MultiMetricValueProvider( int index, String path ) {
			this.index = index;
			this.path = path;
		}
		
		public Float getValue(MultiMetric metric) {
			return metric.getValues()[index];
		}
		public void setValue(MultiMetric object, Float value) {
		}
		public String getPath() {
			return path;
		}
	};
	
	public static ListStore<MultiMetric> buildListStore() {
		return new ListStore<MultiMetric>(keyProvider);	
	}
	
	public static NumericAxis<MultiMetric> buildValueAxis( List<String> labels ) {
		NumericAxis<MultiMetric> valueAxis = buildValueAxis(MultiMetric.class);
		int i=0;
		for( String label : labels ) {
			valueAxis.addField(new MultiMetricValueProvider(i++,label) );
		}
		return valueAxis;
	}
	
	public static TimeAxis<MultiMetric> buildTimeAxis() {
		TimeAxis<MultiMetric> timeAxis = buildTimeAxis( MultiMetric.class );
		timeAxis.setField(dateProvider);
		return timeAxis;
	}
	
	public static List<Series<MultiMetric>> buildValueLineSeries( final List<String> labels ) {
		List<Series<MultiMetric>> ret = new ArrayList<Series<MultiMetric>>();
		int i=0;
		for( String label : labels ) {
			Series<MultiMetric> sm = MultiLineChartBuilder.buildValueSeries(label, i, COLORS[i % COLORS.length]);
			ret.add(sm);
			i++;
		}
		return ret;
	}
	
	public static List<Series<MultiMetric>> buildValueAreaSeries( final List<String> labels ) {
		List<Series<MultiMetric>> ret = new ArrayList<Series<MultiMetric>>( 1 );
		AreaSeries<MultiMetric> series = new AreaSeries<MultiMetric>();
		ret.add( series );
	    // series.setHighlighting(true);
	    series.setYAxisPosition(Position.LEFT);
	    series.setToolTipConfig(getToolTip());
	    
		int i=0;
		for( String label : labels ) {
			series.addYField(new MultiMetricValueProvider(i,label) );
			series.addColor(COLORS[i % COLORS.length]);
			i++;
		}
		return ret;
	}
	
	public static LineSeries<MultiMetric> buildValueSeries( final String label, final int index, Color color ) {
		LineSeries<MultiMetric> series = buildValueSeries( MultiMetric.class, color );
		series.setLegendTitle( label );
		series.setYField( new MultiMetricValueProvider(index) );
	    series.setToolTipConfig(getToolTip());
		return series;
	}
	
	private static SeriesToolTipConfig<MultiMetric> getToolTip() {
		SeriesToolTipConfig<MultiMetric> toolTip = new SeriesToolTipConfig<MultiMetric>();
	    toolTip.setTrackMouse(true);
	    // toolTip.setHideDelay(2);
	    toolTip.setLabelProvider(new SeriesLabelProvider<MultiMetric>() {
	      @Override
	      public String getLabel(MultiMetric item, ValueProvider<? super MultiMetric, ? extends Number> valueProvider) {
	        return dateFormat.format(item.getDate()) + " : " + valueProvider.getPath() + " : " + numberFormat.format(valueProvider.getValue(item));
	      }
	    });
	    return toolTip;
	}
}

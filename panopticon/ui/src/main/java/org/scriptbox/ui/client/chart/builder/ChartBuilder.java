package org.scriptbox.ui.client.chart.builder;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.Legend;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.data.shared.LabelProvider;

public class ChartBuilder {

	protected static final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
	protected static final NumberFormat numberFormat = NumberFormat.getFormat("0.000");
	
	protected static <X> NumericAxis<X> buildValueAxis(Class<X> cls) {
		NumericAxis<X> valueAxis = new NumericAxis<X>();
		valueAxis.setPosition(Position.LEFT);
		valueAxis.setDisplayGrid(true);
		
		valueAxis.setAdjustMaximumByMajorUnit( true );
		valueAxis.setAdjustMinimumByMajorUnit( true );
		
		valueAxis.setLabelProvider(new LabelProvider<Number>() {
			  public String getLabel(Number item) {
			    return numberFormat.format(item.doubleValue());
			  }
			}
		);

		// valueAxis.setMinimum(0); 
		return valueAxis;
	}
	
	public static <X> Legend<X> buildLegend( Class<X> cls ) {
		Legend<X> legend = new Legend<X>();
	    legend.setPosition(Position.BOTTOM);
	    legend.setItemHighlighting(true);
	    legend.setItemHiding(true);
	    return legend;	
	}
	
	protected static <X> TimeAxis<X> buildTimeAxis( Class<X> cls ) {
		TimeAxis<X> timeAxis = new TimeAxis<X>();
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

	protected static <X> LineSeries<X> buildValueSeries( Class<X> cls, Color color ) {
		LineSeries<X> series = new LineSeries<X>();
		series.setYAxisPosition(Position.LEFT);
		series.setStroke(color);
		
		// series.setStroke(new RGB(148, 174, 10));
		// series.setShowMarkers(true);
		// series.setMarkerIndex(1);
		// Sprite marker = Primitives.circle(0, 0, 6);
		// marker.setFill(new RGB(148, 174, 10));
		// series.setMarkerConfig(marker);
		
	    return series;
	}
}

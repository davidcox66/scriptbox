package org.scriptbox.ui.client;

import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.tree.MetricQuery;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.Chart.Position;
import com.sencha.gxt.chart.client.chart.axis.NumericAxis;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.chart.client.chart.series.LineSeries;
import com.sencha.gxt.chart.client.chart.series.Primitives;
import com.sencha.gxt.chart.client.draw.RGB;
import com.sencha.gxt.chart.client.draw.sprite.Sprite;
import com.sencha.gxt.chart.client.draw.sprite.TextSprite;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.loader.Loader;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
 
public class ChartPanel {

	 public interface MetricPropertyAccess extends PropertyAccess<Metric> {
		    ValueProvider<Metric, Date> date();
		 
		    @Path("date")
		    ModelKeyProvider<Metric> nameKey();
		 
		    ValueProvider<Metric, Double> value();
		  }
		 
		  private static final MetricPropertyAccess metricAccess = GWT.create(MetricPropertyAccess.class);
		  private static final DateTimeFormat f = DateTimeFormat.getFormat("HH:mm:ss");
		  // private Timer update;
		 
		  public Widget asWidget() {
		    final Chart<Metric> chart = new Chart<Metric>(600, 400);
		    chart.setDefaultInsets(20);
		 
		    
		    final MetricTreeGWTInterfaceAsync service = GWT.create(MetricTreeGWTInterface.class);
		    
		    RpcProxy<MetricQuery, Metric> proxy = new RpcProxy<MetricQuery, Metric>() {
		        @Override
		        public void load(MetricQuery loadConfig, AsyncCallback<Metric> callback) {
		          service.getMetrics( loadConfig, callback );
		        }
		      };
		   
		    Loader<MetricQuery,Metric> loader = new Loader<MetricQuery,Metric>( proxy );
		    
		    final ListStore<Metric> store = new ListStore<Metric>(metricAccess.nameKey());
		    Date initial = f.parse("Feb 1");
		    for (int i = 0; i < 7; i++) {
		      store.add(new Site(initial, Math.random() * 20 + 80, Math.random() * 20 + 40, Math.random() * 20));
		      initial = CalendarUtil.copyDate(initial);
		      CalendarUtil.addDaysToDate(initial, 1);
		    }
		    chart.setStore(store);
		 
		    NumericAxis<Metric> axis = new NumericAxis<Metric>();
		    axis.setPosition(Position.LEFT);
		    axis.addField(metricAccess.value());
		    TextSprite title = new TextSprite("Value");
		    title.setFontSize(18);
		    axis.setTitleConfig(title);
		    axis.setDisplayGrid(true);
		    axis.setMinimum(0);
		    axis.setMaximum(100);
		    chart.addAxis(axis);
		 
		    final TimeAxis<Metric> time = new TimeAxis<Metric>();
		    time.setField(metricAccess.date());
		    time.setStartDate(f.parse("Feb 1"));
		    time.setEndDate(f.parse("Feb 7"));
		    time.setLabelProvider(new LabelProvider<Date>() {
		      @Override
		      public String getLabel(Date item) {
		        return f.format(item);
		      }
		    });
		    chart.addAxis(time);
		 
		    LineSeries<Metric> series = new LineSeries<Metric>();
		    series.setYAxisPosition(Position.LEFT);
		    series.setYField(metricAccess.value());
		    series.setStroke(new RGB(148, 174, 10));
		    series.setShowMarkers(true);
		    series.setMarkerIndex(1);
		    Sprite marker = Primitives.circle(0, 0, 6);
		    marker.setFill(new RGB(148, 174, 10));
		    series.setMarkerConfig(marker);
		    chart.addSeries(series);
		 
		    /*
		    update = new Timer() {
		      @Override
		      public void run() {
		        Date startDate = CalendarUtil.copyDate(time.getStartDate());
		        Date endDate = CalendarUtil.copyDate(time.getEndDate());
		        CalendarUtil.addDaysToDate(startDate, 1);
		        CalendarUtil.addDaysToDate(endDate, 1);
		        chart.getStore().add(new Site(endDate, Math.random() * 20 + 80, Math.random() * 20 + 40, Math.random() * 20));
		        time.setStartDate(startDate);
		        time.setEndDate(endDate);
		        chart.redrawChart();
		      }
		    };
		 
		    update.scheduleRepeating(1000);
		 
		    ToggleButton animation = new ToggleButton("Animate");
		    animation.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
		      @Override
		      public void onValueChange(ValueChangeEvent<Boolean> event) {
		        chart.setAnimated(event.getValue());
		      }
		    });
		    animation.setValue(true, true);
		    
		    ToolBar toolBar = new ToolBar();
		    toolBar.add(animation);
		 	*/
		 
		    ContentPanel panel = new FramedPanel();
		    panel.getElement().getStyle().setMargin(10, Unit.PX);
		    panel.setCollapsible(true);
		    panel.setHeadingText("Chart");
		    panel.setPixelSize(620, 500);
		    panel.setBodyBorder(true);
		 
		    VerticalLayoutContainer layout = new VerticalLayoutContainer();
		    panel.add(layout);
		 
		    // toolBar.setLayoutData(new VerticalLayoutData(1, -1));
		    // layout.add(toolBar);
		 
		    chart.setLayoutData(new VerticalLayoutData(1, 1));
		    layout.add(chart);
		 
		    /*
		    panel.addAttachHandler(new AttachEvent.Handler() {
		      @Override
		      public void onAttachOrDetach(AttachEvent event) {
		        if (event.isAttached() == false) {
		          update.cancel();
		        }
		      }
		    });
		 	*/
		    
		    return panel;
		  }
}

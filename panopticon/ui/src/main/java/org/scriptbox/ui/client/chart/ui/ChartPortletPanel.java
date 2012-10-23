package org.scriptbox.ui.client.chart.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.configuration.SystemConfiguration;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.ui.client.chart.controller.LineChartController;
import org.scriptbox.ui.client.chart.controller.ReportChartController;
import org.scriptbox.ui.client.chart.model.MultiLineChart;
import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricReportSummaryDto;
import org.scriptbox.ui.shared.chart.MetricTreeDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.info.DefaultInfoConfig;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ChartPortletPanel extends ContentPanel {

	private static final Logger logger = Logger.getLogger("ChartPortletPanel");

	private static final int HEIGHT = 350;
	private static final int HEADER = 5;
	private static final int EXTRA = 20;
	
	private ChartGWTServiceAsync service;
	private PortalLayoutContainer portal;
	private List<Portlet> portlets = new ArrayList<Portlet>();
	private	NumberField<Integer> limit;
	private MetricTreeDto tree;
	
	public ChartPortletPanel( ChartGWTServiceAsync service ) {
		this.service = service;
		setHeadingText("Chart");
		
		VerticalLayoutContainer vertical = new VerticalLayoutContainer();
		buildToolBar( vertical );
		buildPortal( vertical );
		add( vertical );
	}

	public MetricTreeDto getTree() {
		return tree;
	}

	public void setTree(MetricTreeDto tree) {
		this.tree = tree;
	}

	public void load( final MetricTreeNodeDto node ) {
		final LineChartController controller = new LineChartController( service );
		enforceChartLimit( 1, null );
		controller.load( node, new Runnable() {
			public void run() {
				Chart<Metric> chart = controller.getChart().getChart();
				chart.setDefaultInsets(10);
				Portlet portlet = addChartPortlet( node.getId(), chart, HEIGHT );
				addMetricReload( controller, portlet );
			}
		} );
	}

	private void addMetricReload( final LineChartController controller, final Portlet portlet )
	{
		portlet.getHeader().addTool(new ToolButton(ToolButton.REFRESH, new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				controller.reload( null );
			}
		}));
	}
	
	public void load( final MetricReportSummaryDto report ) {
		if( tree != null ) {
			final ReportChartController controller = new ReportChartController( service );
			enforceChartLimit( 1, null );
			controller.load( tree.getTreeName(), report.getName(), new Runnable() {
				public void run() {
					List<MultiLineChart> charts = controller.getCharts();
					VerticalLayoutContainer layout = new VerticalLayoutContainer();
					for( MultiLineChart chart : charts ) {
						Chart<MultiMetric> ch = chart.getChart();
						ch.setDefaultInsets(10);
						
						ContentPanel panel = new ContentPanel();
						panel.setHeadingText(chart.getTitle());
						panel.add( ch );
						layout.add( panel, new VerticalLayoutData(1,HEIGHT) );
					}
					Portlet portlet = addChartPortlet( tree.getTreeName() + " : " + report.getName(), layout, charts.size() * (HEIGHT+HEADER) + EXTRA );
					addReportReload( controller, portlet );
				}
			} );
		}
		else {
			DefaultInfoConfig config = new DefaultInfoConfig( "Error", "No tree selected" );
			config.setDisplay( 5000 );
			Info.display( config );
		}
	}

	private void addReportReload( final ReportChartController controller, final Portlet portlet )
	{
		portlet.getHeader().addTool(new ToolButton(ToolButton.REFRESH, new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				controller.reload( null );
			}
		}));
	}
	
	private void buildToolBar( VerticalLayoutContainer vertical ) {
		ToolBar bar = new ToolBar();
		buildPrint( bar );
		buildRemoveAll( bar );
		buildCollapseAll( bar );
		SeparatorToolItem sep = new SeparatorToolItem();
		sep.setWidth(20);
		bar.add( sep );
		buildLimit( bar );
		vertical.add( bar, new VerticalLayoutData(1,-1) );
	}
	
	private void buildPrint( ToolBar bar ) {
		bar.add(new TextButton("Print", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				print();
			}
		}));
	}
	
	private void buildRemoveAll( ToolBar bar ) {
		bar.add(new TextButton("Remove All", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				for( Portlet portlet : portlets ) {
					portlet.removeFromParent();
				}
				portlets.clear();
			}
		}));
	}
	
	private void buildCollapseAll( ToolBar bar ) {
		bar.add(new TextButton("Collapse All", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				for( Portlet portlet : portlets ) {
					portlet.collapse();
				}
			}
		}));
	}
	
	private void buildLimit( ToolBar bar ) {
		limit = new NumberField<Integer>(new IntegerPropertyEditor());
		limit.setWidth( 50 );
		limit.addChangeHandler( new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				enforceChartLimit(0,null);
			}
		} );
		/*
	    limit.addParseErrorHandler(new ParseErrorHandler() {
	      @Override
	      public void onParseError(ParseErrorEvent event) {
	        Info.display("Parse Error", event.getErrorValue() + " could not be parsed as a number");
	      }
	    });
	    */
	    limit.setAllowBlank(false);
	    
		FieldLabel label = new FieldLabel(limit, "Limit");
		label.setLabelWidth( 40 );
		bar.add( label );
	}
	
	private void buildPortal( VerticalLayoutContainer vertical ) {
		portal = new PortalLayoutContainer(1);
		portal.setColumnWidth(0, 0.99 );
	    portal.getScrollSupport().setScrollMode(ScrollMode.AUTO);
		vertical.add( portal, new VerticalLayoutData(1,1) );
	}
	
	private void print() {
		BodyElement element = getBodyElement();
		element.setInnerHTML( portal.getElement().getInnerHTML() );
	}
	
	public static native BodyElement getBodyElement() /*-{
        var win = window.open("", "win", "width=900,height=400,status=1,resizeable=1,scrollbars=1"); 
        win.document.open("text/html", "print");
		win.document.write("<HTML><HEAD></HEAD><BODY></BODY></HTML>");
        win.document.close(); 
        win.focus();
        return win.document.body;
    }-*/;


	private Portlet addChartPortlet( String title, Widget widget, int height ) {
	    Portlet portlet = new Portlet();
	    portlet.setHeadingText( title );
	    configPortlet( portlet );
	    portlet.add( widget );
	    if( height != 0 ) {
	    	portlet.setHeight( height );
	    }
	    portal.insert(portlet, 0, 0);
		portlets.add( 0, portlet );
		return portlet;
	}
	
	private void enforceChartLimit( int adding, Portlet excluded ) {
		Integer lim = limit.getValue();
		if( lim != null && lim.intValue() > 0 ) {
			int l = lim.intValue() - adding;
			// Only consider open ones for closing
			List<Portlet> opened = new ArrayList<Portlet>();
			for( Portlet portlet : portlets ) {
				if( !portlet.isCollapsed() ) {
					opened.add( portlet );
				}
			}
			// Close enough, ignoring the excluded portlet if specified
			int fromEnd = 0;
			while( opened.size() - fromEnd > l ) {
				int ind = opened.size() - 1 - fromEnd;
				Portlet rm = opened.get( ind );
				if( excluded == null || rm != excluded ) {
					opened.remove(ind);
					portlets.remove(rm);
					rm.removeFromParent();
				}
				else if( rm == excluded ) {
					fromEnd++;
				}
			}
		}
	}
	private void configPortlet(final Portlet panel) {
		panel.setCollapsible(true);
		panel.setAnimCollapse(false);
		
		panel.getHeader().addTool(new ToolButton(ToolButton.CLOSE, new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				panel.removeFromParent();
				portlets.remove( panel );
			}
		}));
		/*
		panel.addExpandHandler( new ExpandHandler() {
			public void onExpand( ExpandEvent event ) {
				enforceChartLimit( 0, panel );
			}
		} );
		*/
	}
}

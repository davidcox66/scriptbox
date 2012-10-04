package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent;
import com.sencha.gxt.widget.core.client.event.ParseErrorEvent.ParseErrorHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.NumberField;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor.IntegerPropertyEditor;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.SeparatorToolItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ChartListPanel extends ContentPanel {

	private static final Logger logger = Logger.getLogger("ChartListPanel");

	private MetricTreeGWTInterfaceAsync service;
	private PortalLayoutContainer portal;
	private List<Portlet> portlets = new ArrayList<Portlet>();
	private	NumberField<Integer> limit;
	
	public ChartListPanel( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		setHeadingText("Chart");
		
		VerticalLayoutContainer vertical = new VerticalLayoutContainer();
		buildToolBar( vertical );
		buildPortal( vertical );
		add( vertical );
	}

	public void load( final MetricTreeNodeDto node ) {
		final SimpleChartController controller = new SimpleChartController( service );
		enforceChartLimit();
		controller.load( node, new Runnable() {
			public void run() {
				Chart<Metric> chart = controller.getChart();
				chart.setDefaultInsets(10);
				addChartPortlet( node, chart );
			}
		} );
	}

	private void buildToolBar( VerticalLayoutContainer vertical ) {
		ToolBar bar = new ToolBar();
		buildRemoveAll( bar );
		SeparatorToolItem sep = new SeparatorToolItem();
		sep.setWidth(20);
		bar.add( sep );
		buildLimit( bar );
		vertical.add( bar, new VerticalLayoutData(1,-1) );
	}
	
	private void buildRemoveAll( ToolBar bar ) {
		bar.add(new TextButton("Remove All", new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				for( Portlet portlet : portlets ) {
					portlet.removeFromParent();
				}
				portlets.clear();
			}
		}));
	}
	
	private void buildLimit( ToolBar bar ) {
		limit = new NumberField<Integer>(new IntegerPropertyEditor());
		limit.setWidth( 50 );
	    limit.addParseErrorHandler(new ParseErrorHandler() {
	 
	      @Override
	      public void onParseError(ParseErrorEvent event) {
	        Info.display("Parse Error", event.getErrorValue() + " could not be parsed as a number");
	      }
	    });
	    limit.setAllowBlank(false);
	    
		FieldLabel label = new FieldLabel(limit, "Limit");
		label.setLabelWidth( 50 );
		bar.add( label );
	}
	
	private void buildPortal( VerticalLayoutContainer vertical ) {
		portal = new PortalLayoutContainer(1);
		portal.setColumnWidth(0, 0.99 );
	    portal.getScrollSupport().setScrollMode(ScrollMode.AUTO);
		vertical.add( portal, new VerticalLayoutData(1,1) );
	}
	
	private void addChartPortlet( MetricTreeNodeDto node, Chart<Metric> chart ) {
	    Portlet portlet = new Portlet();
	    portlet.setHeadingText(node.getId());
	    configPortlet( portlet );
	    portlet.add( chart );
	    portlet.setHeight( 350 );
	    portal.insert(portlet, 0, 0);
		portlets.add( portlet );
	}
	
	private void enforceChartLimit() {
		Integer lim = limit.getValue();
		if( lim != null && lim.intValue() > 0 ) {
			int l = lim.intValue() - 1;
			while( portlets.size() > l ) {
				Portlet rm = portlets.get(portlets.size()-1);
				portlets.remove(portlets.size()-1);
				rm.removeFromParent();
			}
		}
	}
	private void configPortlet(final Portlet panel) {
		panel.setCollapsible(true);
		panel.setAnimCollapse(false);
		panel.getHeader().addTool(new ToolButton(ToolButton.GEAR));
		panel.getHeader().addTool(new ToolButton(ToolButton.CLOSE, new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				panel.removeFromParent();
				portlets.remove( panel );
			}
		}));
	}
}

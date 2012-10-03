package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.Portlet;
import com.sencha.gxt.widget.core.client.button.ToolButton;
import com.sencha.gxt.widget.core.client.container.PortalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

public class ChartListPanel extends ContentPanel {

	private static final Logger logger = Logger.getLogger("ChartListPanel");

	private MetricTreeGWTInterfaceAsync service;
	private PortalLayoutContainer portal;
	
	public ChartListPanel( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		setHeadingText("Chart");
		
		portal = new PortalLayoutContainer(1);
		portal.setColumnWidth(0, 0.99 );
	    portal.getScrollSupport().setScrollMode(ScrollMode.AUTO);
		add( portal );
	}
	
	public void load( MetricTreeNodeDto node ) {
		SimpleChartController controller = new SimpleChartController( service );
		Chart<Metric> chart = controller.getChart();
		chart.setDefaultInsets(20);
		
	    Portlet portlet = new Portlet();
	    portlet.setHeadingText(node.getId());
	    configPanel(portlet);
	    portlet.add( chart );
	    portlet.setHeight( 350 );
	    portal.add(portlet, 0);
		
		controller.load( node );
	}
	
	private void configPanel(final Portlet panel) {
		panel.setCollapsible(true);
		panel.setAnimCollapse(false);
		panel.getHeader().addTool(new ToolButton(ToolButton.GEAR));
		panel.getHeader().addTool(new ToolButton(ToolButton.CLOSE, new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				panel.removeFromParent();
			}
		}));
	}
}

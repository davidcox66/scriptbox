package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.draw.Color;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class ChartListPanel extends ContentPanel {

	private static final Logger logger = Logger.getLogger("ChartListPanel");

	private MetricTreeGWTInterfaceAsync service;
	private VerticalLayoutContainer layout;
	
	public ChartListPanel( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		this.layout = new VerticalLayoutContainer();
		// add(layout);
		setHeadingText("Chart");
	}
	
	public void load( MetricTreeNodeDto node ) {
		SimpleChartController controller = new SimpleChartController( service );
		Chart<Metric> chart = controller.getChart();
		chart.setPixelSize(800,300);
		chart.setDefaultInsets(20);
		// chart.setBackground(new Color("green"));
		
		/*
		ContentPanel chartContent = new ContentPanel();
		chartContent.setHeadingText( node.getId() );
		chartContent.add( chart );
		layout.add(chartContent,new VerticalLayoutData(1, 400));
		*/
		add( chart );
		
		controller.load( node );
	}
}

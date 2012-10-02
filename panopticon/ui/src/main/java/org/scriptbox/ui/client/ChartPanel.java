package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;

import com.google.gwt.dom.client.Style.Unit;
import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class ChartPanel extends ContentPanel {

	private static final Logger logger = Logger.getLogger("ChartPanel");
	
	private SimpleChartController controller;

	public void load( MetricTreeNodeDto node ) {
		controller.load( node );
	}
	
	public ChartPanel() {
		controller = new SimpleChartController();
		Chart<Metric> chart = controller.getChart();
		chart.setPixelSize(600, 400);
		chart.setDefaultInsets(20);

		getElement().getStyle().setMargin(10, Unit.PX);
		setCollapsible(true);
		setHeadingText("Chart");
		setPixelSize(620, 500);
		setBodyBorder(true);

		VerticalLayoutContainer layout = new VerticalLayoutContainer();
		add(layout);

		chart.setLayoutData(new VerticalLayoutData(1, 1));
		layout.add(chart);
	}
}

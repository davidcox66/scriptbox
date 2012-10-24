package org.scriptbox.ui.client.chart.ui;

import org.scriptbox.ui.client.chart.controller.ChartController;

import com.sencha.gxt.widget.core.client.Portlet;

public class ChartPortlet extends Portlet {

	private ChartPortletPanel panel;
	private ChartController controller;
	
	public ChartPortlet( ChartPortletPanel panel, ChartController controller ) {
		this.panel = panel;
		this.controller = controller;
	}

	public ChartPortletPanel getPanel() {
		return panel;
	}

	public ChartController getController() {
		return controller;
	}
	
	
}

package org.scriptbox.ui.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class PanopticonUI implements IsWidget, EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");

	@Override
	public Widget asWidget() {
		ContentPanel treePanel = new ContentPanel();
		treePanel.setHeadingText( "Metrics" );
		treePanel.add( new MetricTreePanel() );
		
		ContentPanel chartPanel = new ContentPanel();
		treePanel.setHeadingText( "Charts" );
		
		VerticalLayoutContainer parent = new VerticalLayoutContainer();
		parent.add(treePanel, new VerticalLayoutData(1, 400));
	    parent.add(chartPanel, new VerticalLayoutData(1, -1));
	    
		return parent;
	}

	public void onModuleLoad() {
		RootPanel.get().add(asWidget());
	}
}

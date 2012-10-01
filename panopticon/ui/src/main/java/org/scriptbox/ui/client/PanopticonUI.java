package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class PanopticonUI implements IsWidget, EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");

	private MetricTreePanel treePanel;
	private ChartPanel chartPanel;
	
	@Override
	public Widget asWidget() {
		treePanel = new MetricTreePanel();
		ContentPanel treeContent = new ContentPanel();
		treeContent.setHeadingText( "Metrics" );
		treeContent.add( treePanel );
		
		chartPanel = new ChartPanel();
		ContentPanel chartContent = new ContentPanel();
		chartContent.setHeadingText( "Charts" );
		chartContent.add( chartPanel );
		
		treePanel.addSelectionHandler(new SelectionHandler<MetricTreeNodeDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricTreeNodeDto> event) {
        	  MetricTreeNodeDto item = event.getSelectedItem();
	          if ( !(item instanceof MetricTreeParentNodeDto) ) {
	        	  chartPanel.load( item );
	          }
	        }
	    });		
		
		VerticalLayoutContainer parent = new VerticalLayoutContainer();
		parent.add(treeContent, new VerticalLayoutData(1, 400));
	    parent.add(chartContent, new VerticalLayoutData(1, -1));
	    
		return parent;
	}

	public void onModuleLoad() {
		RootPanel.get().add(asWidget());
	}
}

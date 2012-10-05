package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;

public class ChartView implements IsWidget {

	private static final Logger logger = Logger.getLogger("ChartView");

	private MetricListPanel listPanel;
	private MetricTreePanel treePanel;
	private ChartListPanel chartListPanel;
	
	public ChartView( MetricTreeGWTInterfaceAsync service ) {
		
		listPanel = new MetricListPanel( service );
		treePanel = new MetricTreePanel( service );
		chartListPanel = new ChartListPanel( service );
		
		listPanel.addSelectionHandler(new SelectionHandler<MetricTreeDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricTreeDto> event) {
        	  MetricTreeDto item = event.getSelectedItem();
        	  logger.info( "Loading tree");
        	  treePanel.load( item );
	        }
	    });		
		
		treePanel.addSelectionHandler(new SelectionHandler<MetricTreeNodeDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricTreeNodeDto> event) {
        	  MetricTreeNodeDto item = event.getSelectedItem();
	          if ( !(item instanceof MetricTreeParentNodeDto) ) {
	        	  logger.info( "Loadding metrics");
	        	  chartListPanel.load( item );
	          }
	        }
	    });		
		
		
	}

	public void load() {
		listPanel.load();
	}
	
	@Override
	public Widget asWidget() {
		VerticalLayoutContainer metricControls = new VerticalLayoutContainer();
		metricControls.add(wrapContent(listPanel,"Trees"), new VerticalLayoutData(1, 0.3));
	    metricControls.add(wrapContent(treePanel,"Metrics"), new VerticalLayoutData(1, 0.7));
	    
		// HorizontalLayoutContainer metricControls = new HorizontalLayoutContainer();
		// metricControls.add(wrapContent(listPanel,"Trees"), new HorizontalLayoutData(250, 1));
	    // metricControls.add(wrapContent(treePanel,"Metrics"), new HorizontalLayoutData(1, 1));
	    
	    BorderLayoutContainer parent = new BorderLayoutContainer();
	    parent.setBorders(true);
	
	    BorderLayoutData controlData = new BorderLayoutData(250);
	    controlData.setCollapsible(true);
	    controlData.setSplit(true);
	    
	    MarginData centerData = new MarginData();
		parent.setWestWidget( metricControls, controlData );
	    parent.setCenterWidget( chartListPanel, centerData );
	    
	    return parent;
	}

	private ContentPanel wrapContent( Widget widget, String title ) {
		ContentPanel content = new ContentPanel();
		content.setHeadingText( title );
		content.add( widget );
		return content;
		
	}
}
package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer.HorizontalLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;

public class PanopticonUI implements IsWidget, EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");

	private MetricListPanel listPanel;
	private MetricTreePanel treePanel;
	private ChartListPanel chartListPanel;
	private MetricTreeGWTInterfaceAsync service; 
	
	@Override
	public Widget asWidget() {
		HorizontalLayoutContainer metricControls = new HorizontalLayoutContainer();
		
		metricControls.add(wrapContent(listPanel,"Trees"), new HorizontalLayoutData(250, 1));
	    metricControls.add(wrapContent(treePanel,"Metrics"), new HorizontalLayoutData(1, 1));
		
		VerticalLayoutContainer parent = new VerticalLayoutContainer();
		parent.add(metricControls, new VerticalLayoutData(1, 250));
	    parent.add(chartListPanel, new VerticalLayoutData(1, 1) );

	    Viewport viewport = new Viewport();
	    viewport.add( parent );
		return viewport;
	}

	public void onModuleLoad() {
		service = GWT.create(MetricTreeGWTInterface.class);
		
		listPanel = new MetricListPanel( service );
		treePanel = new MetricTreePanel( service );
		chartListPanel = new ChartListPanel( service );
		
		listPanel.addSelectionHandler(new SelectionHandler<MetricTreeDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricTreeDto> event) {
        	  MetricTreeDto item = event.getSelectedItem();
        	  logger.info( "Loadding tree");
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
		
		listPanel.load();
		
		RootPanel.get().add(asWidget());
	}
	
	private ContentPanel wrapContent( Widget widget, String title ) {
		ContentPanel content = new ContentPanel();
		content.setHeadingText( title );
		content.add( widget );
		return content;
		
	}
}

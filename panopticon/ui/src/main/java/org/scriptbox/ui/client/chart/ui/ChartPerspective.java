package org.scriptbox.ui.client.chart.ui;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricReportSummaryDto;
import org.scriptbox.ui.shared.chart.MetricTreeDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;
import org.scriptbox.ui.shared.chart.MetricTreeParentNodeDto;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.ExpandMode;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;

public class ChartPerspective implements IsWidget {

	private static final Logger logger = Logger.getLogger("ChartPerspective");

	private MetricListPanel listPanel;
	private MetricTreePanel treePanel;
	private ReportPanel reportPanel;
	private ChartPortletPanel chartListPanel;
	
	public ChartPerspective( ChartGWTServiceAsync service ) {
		
		listPanel = new MetricListPanel( service );
		treePanel = new MetricTreePanel( service );
		reportPanel = new ReportPanel( service );
		chartListPanel = new ChartPortletPanel( service );
		
		listPanel.addSelectionHandler(new SelectionHandler<MetricTreeDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricTreeDto> event) {
        	  logger.info( "Loading tree");
	          reportPanel.clearSelection();
        	  MetricTreeDto item = event.getSelectedItem();
        	  treePanel.load( item );
        	  chartListPanel.setTree( item );
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
		
		reportPanel.addSelectionHandler(new SelectionHandler<MetricReportSummaryDto>() {
	        @Override
	        public void onSelection(SelectionEvent<MetricReportSummaryDto> event) {
        	  MetricReportSummaryDto item = event.getSelectedItem();
        	  logger.info( "Loadding report");
        	  chartListPanel.load( item );
	        }
	    });		
		
		
	}

	public void load() {
		listPanel.load();
		reportPanel.load();
	}
	
	@Override
	public Widget asWidget() {
	    AccordionLayoutContainer metricControls = new AccordionLayoutContainer();
	    metricControls.setExpandMode(ExpandMode.SINGLE_FILL);
	 
	    // AccordionLayoutAppearance appearance = GWT.<AccordionLayoutAppearance> create(AccordionLayoutAppearance.class);
	 
	    Widget listWidget = wrapContent(listPanel,"List");
		metricControls.add(listWidget);
	    metricControls.add(wrapContent(treePanel,"Metrics"));
	    metricControls.add(wrapContent(reportPanel,"Reports"));
	    
	    metricControls.setActiveWidget( listWidget );
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

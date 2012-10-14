package org.scriptbox.ui.client.chart.ui;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricReportSummaryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class ReportPanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("ReportPanel");
	
	public interface MetricReportDtoAccess extends PropertyAccess<MetricReportSummaryDto> {
		@Path("name")
	    ModelKeyProvider<MetricReportSummaryDto> key();
	    ValueProvider<MetricReportSummaryDto, String> name();
	}


	private ChartGWTServiceAsync service; 
	private ListView<MetricReportSummaryDto,String> list;
	private ListStore<MetricReportSummaryDto> store;
	
	public ReportPanel( ChartGWTServiceAsync service ) {
		this.service = service;
		
		MetricReportDtoAccess access = GWT.create(MetricReportDtoAccess.class);
		
		store = new ListStore<MetricReportSummaryDto>(access.key());
		list = new ListView<MetricReportSummaryDto,String>(store,access.name());
		list.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		
		buildToolBar();
	    add(list, new VerticalLayoutData(1, 1));
	    getScrollSupport().setScrollMode(ScrollMode.AUTO);
	}
	
	public void load() {
		service.getReports( new AsyncCallback<ArrayList<MetricReportSummaryDto>>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting trees: " + ex );
			 }
			 public void onSuccess(ArrayList<MetricReportSummaryDto> result) {
				 store.replaceAll( result );
			 }
			
		} );
	}
	
	public void addSelectionHandler( SelectionHandler<MetricReportSummaryDto> handler ) {
		list.getSelectionModel().addSelectionHandler( handler );
	}
	
	private void buildToolBar() {
		ToolBar bar = new ToolBar();
		buildReload( bar );
		add( bar, new VerticalLayoutData(1,-1) );
	}
	
	private void buildReload( ToolBar bar ) {
		bar.add(new TextButton("Reload", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				load();
			}
		}));
	}
	
}

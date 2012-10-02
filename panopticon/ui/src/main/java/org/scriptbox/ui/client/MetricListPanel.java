package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class MetricListPanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("MetricTreePanel");
	
	public interface MetricTreeDtoAccess extends PropertyAccess<MetricTreeDto> {
	    ModelKeyProvider<MetricTreeDto> key();
	    ValueProvider<MetricTreeDto, String> treeName();
	}


	private MetricTreeGWTInterfaceAsync service; 
	private ListView<MetricTreeDto,String> list;
	private ListStore<MetricTreeDto> store;
	
	public MetricListPanel( MetricTreeGWTInterfaceAsync service ) {
		this.service = service;
		
		MetricTreeDtoAccess access = GWT.create(MetricTreeDtoAccess.class);
		
		store = new ListStore<MetricTreeDto>(access.key());
		list = new ListView<MetricTreeDto,String>(store,access.treeName());
		
	    add(list, new VerticalLayoutData(1, 1));
	    getScrollSupport().setScrollMode(ScrollMode.AUTO);
	}
	
	public void load() {
		service.getTrees( new AsyncCallback<ArrayList<MetricTreeDto>>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting trees: " + ex );
			 }
			 public void onSuccess(ArrayList<MetricTreeDto> result) {
				 store.replaceAll( result );
			 }
			
		} );
	}
	
	public void addSelectionHandler( SelectionHandler<MetricTreeDto> handler ) {
		list.getSelectionModel().addSelectionHandler( handler );
	}
	
}

package org.scriptbox.ui.client.chart.ui;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricTreeDto;

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
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.box.ConfirmMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class MetricListPanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("MetricTreePanel");
	
	public interface MetricTreeDtoAccess extends PropertyAccess<MetricTreeDto> {
		@Path("treeName")
	    ModelKeyProvider<MetricTreeDto> key();
	    ValueProvider<MetricTreeDto, String> treeName();
	}


	private ChartGWTServiceAsync service; 
	private ListView<MetricTreeDto,String> list;
	private ListStore<MetricTreeDto> store;
	
	public MetricListPanel( ChartGWTServiceAsync service ) {
		this.service = service;
		
		MetricTreeDtoAccess access = GWT.create(MetricTreeDtoAccess.class);
		
		store = new ListStore<MetricTreeDto>(access.key());
		list = new ListView<MetricTreeDto,String>(store,access.treeName());
		list.getSelectionModel().setSelectionMode( SelectionMode.SINGLE );
		buildToolBar();
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
	
	public void delete() {
		final MetricTreeDto tree = list.getSelectionModel().getSelectedItem();
		if( tree != null ) {
			final HideHandler hideHandler = new HideHandler() {
			      @Override
			      public void onHide(HideEvent event) {
			        Dialog dlg = (Dialog) event.getSource();
			        if( dlg.getHideButton().getText().equals("Yes") ) {
				        
						service.delete( tree, new AsyncCallback<Void>() {
							 public void onFailure(Throwable ex) {
								 logger.info( "Failed deleting tree: " + ex );
							 }
							 public void onSuccess(Void result) {
								 store.remove( tree );
								 Info.display("Deleted", "Tree deleted: " + tree.getTreeName() );
							 }
						} );
			        }
			      }
			};
			    
			ConfirmMessageBox box = new ConfirmMessageBox("Delete", "Are you sure you want to delete " + tree.getTreeName() + "?" );
	        box.addHideHandler(hideHandler);
	        box.show();
		}
	}
	
	public void addSelectionHandler( SelectionHandler<MetricTreeDto> handler ) {
		list.getSelectionModel().addSelectionHandler( handler );
	}
	
	private void buildToolBar() {
		ToolBar bar = new ToolBar();
		buildReload( bar );
		buildDelete( bar );
		add( bar, new VerticalLayoutData(1,-1) );
	}
	
	private void buildReload( ToolBar bar ) {
		bar.add(new TextButton("Reload", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				load();
			}
		}));
	}
	
	private void buildDelete( ToolBar bar ) {
		bar.add(new TextButton("Delete", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				delete();
			}
		}));
	}
}

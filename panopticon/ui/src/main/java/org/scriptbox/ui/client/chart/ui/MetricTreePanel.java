package org.scriptbox.ui.client.chart.ui;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricTreeDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;
import org.scriptbox.ui.shared.chart.MetricTreeParentNodeDto;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.Store;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.StoreFilterField;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

public class MetricTreePanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("MetricTreePanel");

	private ChartGWTServiceAsync service; 
	private Tree<MetricTreeNodeDto,String> tree;
	private TreeStore<MetricTreeNodeDto> store;
	private MetricTreeDto treeDto;
	
	public void addSelectionHandler( SelectionHandler<MetricTreeNodeDto> handler ) {
		tree.getSelectionModel().addSelectionHandler( handler );
	}
	
	public MetricTreePanel( ChartGWTServiceAsync service ) {
		this.service = service;
		buildStore();
		buildToolBar();
		buildFilter();
		buildTree();
	    getScrollSupport().setScrollMode(ScrollMode.AUTO);
	}

	private void buildStore() {
		store = new TreeStore<MetricTreeNodeDto>(new ModelKeyProvider<MetricTreeNodeDto>() {
			public String getKey(MetricTreeNodeDto item) {
				return (item instanceof MetricTreeParentNodeDto ? "f-" : "m-")
						+ item.getId().toString();
			} 
		});
	}
	
	private void buildToolBar() {
		ToolBar buttonBar = new ToolBar();
		buttonBar.add(new TextButton("Reload", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				load();
			}
		}));
		buttonBar.add(new TextButton("Expand All", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				tree.expandAll();
			}
		}));
		buttonBar.add(new TextButton("Collapse All", new SelectHandler() {
			public void onSelect(SelectEvent event) {
				tree.collapseAll();
			}
		}));
		add(buttonBar, new VerticalLayoutData(1, -1));
	}

	private void buildFilter() {
	    StoreFilterField<MetricTreeNodeDto> filter = new StoreFilterField<MetricTreeNodeDto>() {  
	    	  
	        @Override  
	        protected boolean doSelect(Store<MetricTreeNodeDto> store, MetricTreeNodeDto parent,  MetricTreeNodeDto item, String filter) {  
	       
	          String[] parts = filter.split("[ 	]+");
	          for( int i=0 ; i < parts.length ; i++ ) {
	        	  parts[i] = parts[i].toLowerCase();
	          }
        	  MetricTreeNodeDto node = item;
        	  while( node != null ) {
        		  String name = node.getName().toLowerCase(); 
        		  for( int i=0 ; i < parts.length ; i++ ) {
	        		  if( name.indexOf(parts[i]) != -1 ) {
	        			  return true;
	        		  }
	        	  }
        		  node = node.getParent();
	          }  
	          return false;  
	        }  
	    
	      };  
	      filter.bind(store);  	
	      add(filter, new VerticalLayoutData(1, -1));
	}
	private void buildTree() {
		tree = new Tree<MetricTreeNodeDto, String>(store,
			new ValueProvider<MetricTreeNodeDto, String>() {
				public String getValue(MetricTreeNodeDto node) {
					return node.getName();
				}
				public void setValue(MetricTreeNodeDto object, String value) {
				}
				public String getPath() {
					return "name";
				}
		});
	    add(tree, new VerticalLayoutData(1, 1));
	}
	
	public void load() {
		if( treeDto != null ) {
			load( treeDto );
		}
	}
	
	public void load( MetricTreeDto treeDto ) {
		this.treeDto = treeDto;
		AsyncCallback<MetricTreeParentNodeDto> callback = new AsyncCallback<MetricTreeParentNodeDto>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting tree: " + ex );
			 }
			 public void onSuccess(MetricTreeParentNodeDto root) {
				 store.clear();
				 store.add( root );
				 processFolder( store, root );
			 }
		};
		service.getRoot( treeDto, callback );
	}
	private void processFolder(TreeStore<MetricTreeNodeDto> store, MetricTreeParentNodeDto folder) {
		for (MetricTreeNodeDto child : folder.getChildren()) {
			store.add(folder, child);
		    if (child instanceof MetricTreeParentNodeDto) {
		    	processFolder(store, (MetricTreeParentNodeDto) child);
		    }
		}
	}
	
}

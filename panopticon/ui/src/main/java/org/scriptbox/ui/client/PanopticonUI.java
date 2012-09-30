package org.scriptbox.ui.client;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import com.sencha.gxt.widget.core.client.tree.Tree;

public class PanopticonUI implements IsWidget, EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");

	class KeyProvider implements ModelKeyProvider<MetricTreeNodeDto> {
		@Override
		public String getKey(MetricTreeNodeDto item) {
			return (item instanceof MetricTreeParentNodeDto ? "f-" : "m-")
					+ item.getId().toString();
		}
	}

	@Override
	public Widget asWidget() {
		ContentPanel panel = new ContentPanel();
		panel.setHeadingText("Async Tree");
		panel.setPixelSize(315, 400);
		panel.addStyleName("margin-10");

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		panel.add(con);

		final TreeStore<MetricTreeNodeDto> store = new TreeStore<MetricTreeNodeDto>(new KeyProvider());

		final Tree<MetricTreeNodeDto, String> tree = new Tree<MetricTreeNodeDto, String>(store,
			new ValueProvider<MetricTreeNodeDto, String>() {

				@Override
				public String getValue(MetricTreeNodeDto node) {
					return node.getName();
				}

				@Override
				public void setValue(MetricTreeNodeDto object, String value) {
				}

				@Override
				public String getPath() {
					return "name";
				}
			});

		
		final AsyncCallback<MetricTreeParentNodeDto> cb = new AsyncCallback<MetricTreeParentNodeDto>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting tree: " + ex );
			 }
			 public void onSuccess(MetricTreeParentNodeDto root) {
				 for (MetricTreeNodeDto base : root.getChildren()) {
				      store.add(base);
				      if (base instanceof MetricTreeParentNodeDto ) {
				        processFolder(store, (MetricTreeParentNodeDto) base);
				      }
				    }
				 
			 }
			
		};
		
		final MetricTreeGWTInterfaceAsync service = GWT.create(MetricTreeGWTInterface.class);
		service.getTrees( new AsyncCallback<ArrayList<MetricTreeDto>>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting trees: " + ex );
			 }
			 public void onSuccess(ArrayList<MetricTreeDto> result) {
				 service.getRoot( (MetricTreeDto)result.get(0), cb );
			 }
			
		} );

		// TreeStateHandler<MetricTreeNodeDto> stateHandler = new TreeStateHandler<MetricTreeNodeDto>( tree);
		// stateHandler.loadState();
		// tree.getStyle().setLeafIcon(ExampleImages.INSTANCE.music());

		ToolBar buttonBar = new ToolBar();

		buttonBar.add(new TextButton("Expand All", new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				tree.expandAll();
			}
		}));
		buttonBar.add(new TextButton("Collapse All", new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				tree.collapseAll();
			}

		}));

		con.add(buttonBar, new VerticalLayoutData(1, -1));
		con.add(tree, new VerticalLayoutData(1, 1));

		return panel;
	}

	public void onModuleLoad() {
		RootPanel.get().add(asWidget());
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

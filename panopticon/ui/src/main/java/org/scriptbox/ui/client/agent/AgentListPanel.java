package org.scriptbox.ui.client.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.scriptbox.ui.shared.agent.Agent;
import org.scriptbox.ui.shared.agent.AgentGWTServiceAsync;
import org.scriptbox.ui.shared.chart.MetricTreeDto;
import org.scriptbox.util.cassandra.heartbeat.Heartbeat;
import org.scriptbox.util.remoting.endpoint.Endpoint;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.IdentityValueProvider;
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
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class AgentListPanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("AgentListPanel");
	
	public interface Access extends PropertyAccess<Agent> {
		@Path("id")
	    ModelKeyProvider<Agent> key();
	    ValueProvider<Agent, String> id();
	    ValueProvider<Agent, String> group();
	    ValueProvider<Agent, String> type();
	}

	private static ValueProvider<Agent,String> infoProvider = new ValueProvider<Agent,String>() {
		public String getValue(Agent heartbeat) {
			return heartbeat.getData();
		}
		public void setValue(Agent object, String value) {
		}
		public String getPath() {
			return "data";
		}
	};
	
	private static ValueProvider<Agent,String> tagsProvider = new ValueProvider<Agent,String>() {
		public String getValue(Agent heartbeat) {
			StringBuilder builder = new StringBuilder();
			for( String tag : heartbeat.getTags() ) {
				if( builder.length() > 0 ) {
					builder.append( "," );
				}
				builder.append( tag );
			}
			return builder.toString();
		}
		public void setValue(Agent object, String value) {
		}
		public String getPath() {
			return "tags";
		}
	};
	
	
	private AgentGWTServiceAsync service; 
	private Grid<Agent> grid;
	private ListView<MetricTreeDto,String> list;
	private ListStore<Agent> store;
	
	public AgentListPanel( AgentGWTServiceAsync service ) {
		this.service = service;
		
		Access access = GWT.create(Access.class);
		
		store = new ListStore<Agent>(access.key());

		ColumnConfig<Agent, String> idCol = new ColumnConfig<Agent, String>(access.id(), 200, "ID");
	    ColumnConfig<Agent, String> groupCol = new ColumnConfig<Agent, String>(access.group(), 100, "Group");
	    ColumnConfig<Agent, String> typeCol = new ColumnConfig<Agent, String>(access.type(), 75, "Type");
	    ColumnConfig<Agent, String> tagsCol = new ColumnConfig<Agent, String>(tagsProvider, 75, "Tags");
	    ColumnConfig<Agent, String> infoCol = new ColumnConfig<Agent, String>(infoProvider, 75, "Information");

	    List<ColumnConfig<Agent, ?>> colList = new ArrayList<ColumnConfig<Agent, ?>>();
	    colList.add( idCol );
	    colList.add( groupCol );
	    colList.add( typeCol );
	    colList.add( tagsCol );
	    colList.add( infoCol );
	    ColumnModel<Agent> colModel = new ColumnModel<Agent>(colList);
	    
	    IdentityValueProvider<Agent> identity = new IdentityValueProvider<Agent>();
	    
	    final CheckBoxSelectionModel<Agent> selectionModel = new CheckBoxSelectionModel<Agent>(identity);
	    grid = new Grid<Agent>(store, colModel);
	    grid.setSelectionModel(selectionModel);
	    grid.getView().setAutoExpandColumn(tagsCol);
	    grid.setBorders(false);
	    grid.getView().setStripeRows(true);
	    grid.getView().setColumnLines(true);
	    
	    buildToolBar();
	    add(grid, new VerticalLayoutData(1, 1));
	    getScrollSupport().setScrollMode(ScrollMode.AUTO);
	}
	
	public void load() {
		List<String> groups = new ArrayList<String>();
		groups.add( "horde" );
		groups.add( "panopticon" );
		service.getAgents(groups, new AsyncCallback<List<Agent>>() {
			 public void onFailure(Throwable ex) {
				 logger.info( "Failed getting agents: " + ex );
			 }
			 public void onSuccess(List<Agent> result) {
				 store.replaceAll( result );
			 }
		} );
	}
	
	public void addSelectionHandler( SelectionHandler<MetricTreeDto> handler ) {
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

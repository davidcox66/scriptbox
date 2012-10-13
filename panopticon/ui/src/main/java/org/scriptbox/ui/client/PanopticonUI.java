package org.scriptbox.ui.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.scriptbox.ui.client.chart.ui.ChartPerspective;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterfaceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.container.Viewport;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuBar;
import com.sencha.gxt.widget.core.client.menu.MenuBarItem;
import com.sencha.gxt.widget.core.client.menu.MenuItem;

public class PanopticonUI implements IsWidget, EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");

	private CardLayoutContainer cards;
	private ChartPerspective chartView;
	private Menu viewMenu ;
	private MetricTreeGWTInterfaceAsync service; 
	
	@Override
	public Widget asWidget() {
		VerticalLayoutContainer parent = new VerticalLayoutContainer();
		MenuBar tools = new MenuBar();
		
		viewMenu = new Menu();
		tools.add( new MenuBarItem("View", viewMenu) );
		
		cards = new CardLayoutContainer();	
		parent.add( tools, new VerticalLayoutData(1,-1) );
		parent.add( cards, new VerticalLayoutData(1,1) );
		
		buildViewItem( "Charts", chartView.asWidget() );
		buildViewItem( "Agents", new ContentPanel() );
		buildViewItem( "Maintenance", new ContentPanel() );
		
		return parent;
	    
	}

	private void buildViewItem( String label, final Widget view ) {
		cards.add( view );
		MenuItem agentsItem = new MenuItem( label );
		viewMenu.add( agentsItem );
		agentsItem.addSelectionHandler( new SelectionHandler<Item>() {
			public void onSelection( SelectionEvent<Item> event ) {
				cards.setActiveWidget( view );
			}
		} );
		
	}
	public void onModuleLoad() {
		GWT.setUncaughtExceptionHandler( new UncaughtExceptionHandler() {
			public void onUncaughtException(Throwable ex ) {
				logger.log( Level.SEVERE, "Uncaught exception", ex ); 
			}
		} );
		service = GWT.create(MetricTreeGWTInterface.class);
		chartView = new ChartPerspective( service );
		chartView.load();
		
	    Viewport viewport = new Viewport();
	    viewport.add( asWidget() );
		RootPanel.get().add( viewport );
	}
}

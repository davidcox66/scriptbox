package org.scriptbox.ui.client.agent;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.agent.AgentGWTServiceAsync;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.ExpandMode;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;

public class AgentPerspective implements IsWidget {

	private static final Logger logger = Logger.getLogger("AgentPerspective");

	private AgentListPanel listPanel;
	private AgentOutputPanel outputPanel;
	private AgentScriptPanel scriptPanel;
	
	public AgentPerspective( AgentGWTServiceAsync service ) {
		
		listPanel = new AgentListPanel( service );
		outputPanel = new AgentOutputPanel( service );
		scriptPanel = new AgentScriptPanel( service );
	}

	public void load() {
		listPanel.load();
	}
	
	@Override
	public Widget asWidget() {
	    BorderLayoutContainer parent = new BorderLayoutContainer();
	    parent.setBorders(true);
	
	    BorderLayoutData scriptData = new BorderLayoutData(250);
	    scriptData.setCollapsible(true);
	    scriptData.setSplit(true);
	    
	    BorderLayoutData outputData = new BorderLayoutData(250);
	    outputData.setCollapsible(true);
	    outputData.setSplit(true);
	    
	    MarginData centerData = new MarginData();
		parent.setWestWidget( wrapContent(scriptPanel, "Scripts"), scriptData );
		parent.setCenterWidget( wrapContent(listPanel, "Agents"), centerData );
		parent.setSouthWidget( wrapContent(outputPanel, "Output"), outputData );
	    
	    return parent;
	}

	private ContentPanel wrapContent( Widget widget, String title ) {
		ContentPanel content = new ContentPanel();
		content.setHeadingText( title );
		content.add( widget );
		return content;
		
	}
}

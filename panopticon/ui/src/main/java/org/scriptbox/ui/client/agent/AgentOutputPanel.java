package org.scriptbox.ui.client.agent;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.agent.AgentGWTServiceAsync;

import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;

public class AgentOutputPanel extends VerticalLayoutContainer {

	private static final Logger logger = Logger.getLogger("AgentOutputPanel");
	
	private AgentGWTServiceAsync service; 
	
	public AgentOutputPanel( AgentGWTServiceAsync service ) {
		this.service = service;
	}
}

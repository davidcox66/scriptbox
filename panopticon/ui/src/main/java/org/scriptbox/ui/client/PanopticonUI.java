package org.scriptbox.ui.client;

import java.util.logging.Logger;

import org.scriptbox.ui.shared.ClientGWTInterfaceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.sencha.gxt.widget.core.client.box.MessageBox;

public class PanopticonUI implements EntryPoint {

	private static final Logger logger = Logger.getLogger("PanopticonUI");
    
	private ClientGWTInterfaceAsync client;
	
	@Override
	public void onModuleLoad() {
		MessageBox popup = new MessageBox("On the 8th day, God said...","Hello World");
		RootPanel.get().add(popup);
		popup.show();
		 
		// RootLayoutPanel viewRoot = RootLayoutPanel.get();
		 
		// client = (ClientGWTInterfaceAsync) GWT.create( ClientGWTInterface.class );
		// retriever = (ImageRetrieverGWTInterfaceAsync) GWT.create( ImageRetrieverGWTInterface.class );
		
		/*
		client.getCredentials(new AsyncCallback<ClientData>() {
			public void onSuccess( ClientData data ) {
				GWT.log( "Got test client data" );
				initialize( data.getClientId(), data.getKeyId(), data.getSalt(), data.getSecretCredentials() );
			}
			public void onFailure( Throwable ex ) {
				GWT.log( "Client data failure: " + ex );
			}
		} );
		*/
	
	}
}

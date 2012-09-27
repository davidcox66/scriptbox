package org.scriptbox.ui.server;

import org.scriptbox.ui.shared.ClientData;
import org.scriptbox.ui.shared.ClientGWTInterface;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("client")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="client",scope=ServiceScope.SESSION)
public class Client implements ClientGWTInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( Client.class );
	
	@Override
	public ClientData getCredentials() {
		try {
			ClientData data = new ClientData();
			return data;
		}
		catch( Exception ex ) {
			LOGGER.error( "Error generating test request", ex );
			throw new RuntimeException( "Error generating test request", ex );
		}
	}

}

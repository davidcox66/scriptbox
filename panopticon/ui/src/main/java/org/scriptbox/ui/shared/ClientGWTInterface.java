package org.scriptbox.ui.shared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/client")
public interface ClientGWTInterface extends RemoteService {

	public ClientData getCredentials();
}

package org.scriptbox.util.gwt.server.remote.factory;

import javax.servlet.http.HttpServletRequest;

public interface ServiceFactory {

	public Object getInstance( HttpServletRequest request ) throws Exception;
}

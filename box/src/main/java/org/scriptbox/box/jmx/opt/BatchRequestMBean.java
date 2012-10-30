package org.scriptbox.box.jmx.opt;

import java.rmi.RemoteException;

public interface BatchRequestMBean {

	public BatchResponse process( BatchRequest request ) throws RemoteException;
}

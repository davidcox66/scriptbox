package org.scriptbox.ui.shared;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/treeService")
public interface MetricTreeGWTInterface extends RemoteService {

	public ArrayList<TreeDto> getTrees();
	public TreeParentNodeDto getRoot( TreeDto tree );
}

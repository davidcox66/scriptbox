package org.scriptbox.ui.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/tree")
public interface MetricTreeGWTInterface extends RemoteService {

	public List<TreeDto> getTrees();
	public TreeParentNodeDto getRoot( TreeDto tree );
}

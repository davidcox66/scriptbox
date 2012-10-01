package org.scriptbox.ui.shared.tree;

import java.util.ArrayList;
import java.util.List;

import org.scriptbox.metrics.model.Metric;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/treeService")
public interface MetricTreeGWTInterface extends RemoteService {

	public ArrayList<MetricTreeDto> getTrees();
	public MetricTreeParentNodeDto getRoot( MetricTreeDto tree );
	public List<Metric> getMetrics( MetricQuery query );
}

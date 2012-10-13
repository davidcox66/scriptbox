package org.scriptbox.ui.shared.tree;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/treeService")
public interface ChartGWTService extends RemoteService {

	public ArrayList<MetricTreeDto> getTrees();
	public MetricTreeParentNodeDto getRoot( MetricTreeDto tree );
	public MetricRangeDto getMetrics( MetricQueryDto query );
	public void delete( MetricTreeDto tree );
	
	public ArrayList<MetricReportSummaryDto> getReports();
	public MetricReportDto getReport( ReportQueryDto query ) throws Exception;
	
	
}

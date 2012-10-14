package org.scriptbox.ui.shared.chart;

import java.util.ArrayList;

import org.scriptbox.util.gwt.server.remote.shared.ServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("remote/treeService")
public interface ChartGWTService extends RemoteService {

	public ArrayList<MetricTreeDto> getTrees() throws ServiceException;
	public MetricTreeParentNodeDto getRoot( MetricTreeDto tree ) throws ServiceException;
	public MetricRangeDto getMetrics( MetricQueryDto query ) throws ServiceException;
	public void delete( MetricTreeDto tree ) throws ServiceException;
	
	public ArrayList<MetricReportSummaryDto> getReports() throws ServiceException;
	public MetricReportDto getReport( ReportQueryDto query ) throws ServiceException;
	
	
}

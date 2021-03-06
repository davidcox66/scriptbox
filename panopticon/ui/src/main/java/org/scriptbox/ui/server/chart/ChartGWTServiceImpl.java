package org.scriptbox.ui.server.chart;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.compute.MetricCollator;
import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.metrics.model.MultiMetric;
import org.scriptbox.metrics.query.groovy.Report;
import org.scriptbox.metrics.query.groovy.ReportBuilder;
import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.ui.shared.chart.ChartGWTService;
import org.scriptbox.ui.shared.chart.MetricDescriptionDto;
import org.scriptbox.ui.shared.chart.MetricQueryDto;
import org.scriptbox.ui.shared.chart.MetricRangeDto;
import org.scriptbox.ui.shared.chart.MetricReportDto;
import org.scriptbox.ui.shared.chart.MetricReportSummaryDto;
import org.scriptbox.ui.shared.chart.MetricTreeDto;
import org.scriptbox.ui.shared.chart.MetricTreeNodeDto;
import org.scriptbox.ui.shared.chart.MetricTreeParentNodeDto;
import org.scriptbox.ui.shared.chart.MultiMetricRangeDto;
import org.scriptbox.ui.shared.chart.ReportQueryDto;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.scriptbox.util.gwt.server.remote.shared.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

// @Service("treeService")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="treeService",scope=ServiceScope.SESSION)
public class ChartGWTServiceImpl implements ChartGWTService {

	private static final Logger LOGGER = LoggerFactory.getLogger( ChartGWTServiceImpl.class );
	
	private MetricStore store;
	private List<String> reportPaths;
	private List<MetricDescriptionPostProcessor> metricDescriptionProcessors;
	
	private Map<String,MetricTree> trees;
	private Map<MetricTree,Map<String,MetricTreeNode>> allNodes = new HashMap<MetricTree,Map<String,MetricTreeNode>>();
	private Map<String,Report> reports;
	
	public MetricStore getStore() {
		return store;
	}

	public void setStore(MetricStore store) {
		this.store = store;
	}

	public List<String> getReportPaths() {
		return reportPaths;
	}

	public void setReportPaths(List<String> reportPaths) {
		this.reportPaths = reportPaths;
	}

	
	public List<MetricDescriptionPostProcessor> getMetricDescriptionProcessors() {
		return metricDescriptionProcessors;
	}

	public void setMetricDescriptionProcessors(
			List<MetricDescriptionPostProcessor> metricDescriptionProcessors) {
		this.metricDescriptionProcessors = metricDescriptionProcessors;
	}

	@Override
	public ArrayList<MetricTreeDto> getTrees() throws ServiceException {
		store.begin();
		try {
			List<MetricTree> tr = store.getAllMetricTrees();
			ArrayList<MetricTreeDto> ret = new ArrayList<MetricTreeDto>();
			trees = new HashMap<String,MetricTree>();
			for( MetricTree tree : tr ) {
				trees.put( tree.getName(), tree );
				ret.add( new MetricTreeDto(tree.getName()) );
			}
			return ret;
		}
		finally {
			store.end();
		}
	}

	public MetricTreeParentNodeDto getRoot( MetricTreeDto treeDto ) throws ServiceException {
		store.begin();
		try {
			MetricTree tree = loadTreeByName(treeDto.getTreeName());
			MetricTreeNode root = tree.getRoot();
			
			Map<String,MetricTreeNode> nodes = new HashMap<String,MetricTreeNode>();
			MetricTreeParentNodeDto ret = (MetricTreeParentNodeDto)populateNodes( nodes, tree, root, null );
			allNodes.put( tree, nodes );
			return ret;
		}
		finally {
			store.end();
		}
	}
	
	public void delete( MetricTreeDto treeDto ) throws ServiceException {
		MetricTree tree = loadTreeByName(treeDto.getTreeName());
		tree.delete();
	}
	
	public MetricRangeDto getMetrics( MetricQueryDto query ) {
		store.begin();
		try {
			MetricTree tree = getTreeByName( query.getNode().getTreeName() );
			Map<String,MetricTreeNode> nodes = allNodes.get( tree );
			if( nodes == null ) {
				throw new RuntimeException( "Tree nodes not found: '" + tree.getName() );
			}
			MetricTreeNode node = nodes.get( query.getNode().getId() ); 
			if( node == null ) {
				throw new RuntimeException( "Node not found: '" + query.getNode().getId() );
			}
			MetricSequence seq = node.getMetricSequence();
			// MetricRange range = seq.getRange(query.getStart().getTime(), query.getEnd().getTime(), 30 );
			DateRange dr = seq.getFullDateRange();
			Date qstart = query.getStart() != null ? query.getStart() : new Date(Long.MIN_VALUE);
			Date qend = query.getEnd() != null ? query.getEnd() : new Date(Long.MAX_VALUE);
			long start = Math.max(dr.getStart().getTime(),qstart.getTime());
			long end = Math.min(dr.getEnd().getTime(),qend.getTime());
			
			if( LOGGER.isDebugEnabled() ) {
				LOGGER.debug( "getMetrics: seq.start=" + dr.getStart() + ", seq.end=" + dr.getEnd() +
					", query.start=" + query.getStart() + ", query.end=" + query.getEnd() +
					", start=" + new Date(start) + ", end=" + new Date(end) + ", resolution=" + query.getResolution() );
			}
			MetricRange range = seq.getRange( start, end );
			MetricRangeDto ret = new MetricRangeDto( 
				dr.getStart().getTime(), 
				dr.getEnd().getTime(), 
				range.getStart(), 
				range.getEnd(), 
				range.getMetrics(query.getResolution()) );
			if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "getMetrics: node=" + node + ", metrics.size()=" + ret.getData().size() ); }
			return ret;
		}
		finally {
			store.end();
		}
	}

	public ArrayList<MetricReportSummaryDto> getReports() throws ServiceException {
		reports = new HashMap<String,Report>();
		ArrayList<MetricReportSummaryDto> ret = new ArrayList<MetricReportSummaryDto>();
		if( reportPaths == null || reportPaths.size() == 0 ) {
			LOGGER.warn( "getReports: no report paths found" );
		}
		for( String reportPath : reportPaths ) {
			File reportPathFile = new File( reportPath );
			if( reportPathFile.exists() && reportPathFile.isDirectory() ) {
				LOGGER.debug( "getReports: scanning " + reportPathFile );
				File[] reportFiles = reportPathFile.listFiles( new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".groovy");
					}
				});
				for( File reportFile : reportFiles ) {
					try {
						LOGGER.debug( "getReports: loading " + reportFile );
						ReportBuilder builder = new ReportBuilder( reportFile );
						Report report = builder.compile();
						reports.put( report.getName(), report );
						
						LOGGER.debug( "getReports: loaded report " + report.getName() );
						ArrayList<String> charts = new ArrayList<String>();
						for( ReportElement element : report.getElements() ) {
							// LOGGER.debug( "getReports: processing chart " + element.getTitle() );
							charts.add( element.getTitle() );
						}
						MetricReportSummaryDto summaryDto = new MetricReportSummaryDto();
						summaryDto.setName( report.getName () );
						summaryDto.setCharts( charts );
						ret.add( summaryDto );
					}
					catch( Exception ex ) {
						LOGGER.error( "getReports: failed loading report: " + reportFile, ex );
					}
				}
			}
			else {
				LOGGER.warn( "getReports: report directory does not exist: " + reportPath );
			}
		}
		return ret;
	}
	
	public MetricReportDto getReport( ReportQueryDto query ) throws ServiceException {
		
		store.begin();
		try {
			MetricTree tree = getTreeByName( query.getTreeName() );
			Report report = getReportByName( query.getReportName() );
		
			if( LOGGER.isDebugEnabled() ) {
				LOGGER.debug( "getReport: tree=" + tree.getName() + ", report=" + report.getName() );
			}
			long first = Long.MAX_VALUE;
			long last = Long.MIN_VALUE;
			
			Date qstart = query.getStart() != null ? query.getStart() : new Date(Long.MIN_VALUE);
			Date qend = query.getEnd() != null ? query.getEnd() : new Date(Long.MAX_VALUE);
			Date start = report.getStart() != null ? report.getStart() : qstart;
			Date end =	report.getEnd() != null ? report.getEnd() : qend;
			int resolution = report.getResolution() > 0 ? report.getResolution() : query.getResolution();
			
			if( LOGGER.isDebugEnabled() ) {
				LOGGER.debug( "getReport: " +
					"report.start=" + report.getStart() + ", report.end=" + report.getEnd() + ", report.resolution=" + report.getResolution() +
					", query.start=" + query.getStart() + ", query.end=" + query.getEnd() + ", query.resolution=" + query.getResolution() + 
					", start=" + start + ", end=" + end + ", resolution=" + resolution );
			}
			MetricReportDto reportDto = new MetricReportDto();
			for( ReportElement element : report.getElements() ) {
				Map<? extends MetricProvider,? extends MetricRange> result = MetricQueries.query(
					store, tree, element.getExpression(), start, end, resolution ); 
	
				if( LOGGER.isDebugEnabled() ) {
					LOGGER.debug( "getReport: result.size()=" + result.size() );
					for( Map.Entry<? extends MetricProvider,? extends MetricRange> entry : result.entrySet() ) {
						LOGGER.debug( "getReport: provider=" + entry.getKey() + ", range=" + entry.getValue() );
					}
				}
			
				//
				// Get an ordered list of both the names and corresponding list of metrics so that
				// we can properly associate these for displaying information about each
				// series in the UI (such as tooltips)
				//
				List<MetricDescriptionDto> descriptions = new ArrayList<MetricDescriptionDto>(result.size());
				List<MetricRange> ranges = new ArrayList<MetricRange>(result.size());
				for( Map.Entry<? extends MetricProvider,? extends MetricRange> entry : result.entrySet() ) {
					descriptions.add( new MetricDescriptionDto(entry.getKey().getId()) );
					ranges.add( entry.getValue() );
				}
				processDescriptions( element, descriptions );
				
				final List<MultiMetric> metrics = toMultiMetrics( element, ranges );
				
				// Compute the overall date range of the report
				if( metrics.size() > 0 ) {
					first = Math.min(first, metrics.get(0).getMillis() );
					last = Math.max(last, metrics.get(metrics.size()-1).getMillis() );
				}
				MultiMetricRangeDto range = new MultiMetricRangeDto( element.getTitle(), descriptions, metrics,
					(String)((Map)element.getParams()).get("presentation") );
				reportDto.addChart( range );
			}
			reportDto.setStart( new Date(first) );
			reportDto.setEnd( new Date(last) );
			reportDto.setFirst( new Date(first) );
			reportDto.setLast( new Date(last) );
			
			LOGGER.debug( "getReport: finished generating report: " + report.getName() + ", tree: " + query.getTreeName() );
			return reportDto;
		}
		catch( Exception ex ) {
			LOGGER.debug( "getReport: error generating report: " + query.getReportName() + ", tree: " + query.getTreeName(), ex );
			throw new ServiceException( ex.getMessage() );
		}
		finally {
			store.end();
		}
	}

	private void processDescriptions( ReportElement element, List<MetricDescriptionDto> descriptions )
	{
		if( metricDescriptionProcessors != null ) {
			for( MetricDescriptionPostProcessor proc : metricDescriptionProcessors ) {
				proc.process( element, descriptions );
			}
		}
	}
	
	private List<MultiMetric> toMultiMetrics( ReportElement element, List<MetricRange> ranges ) throws Exception {
		//
		// Convert the multiple ranges into a single range of MultiMetric where each MultiMetric
		// has values from each of the original ranges. This format is more acceptable to the 
		// GXT charting APIs
		//
		final List<MultiMetric> metrics = new ArrayList<MultiMetric>( 128 ); // Just a reasonable starting point
		MetricCollator collator = new MetricCollator( element.getTitle(), element.getTitle(), 30, ranges );
		collator.multi( new ParameterizedRunnable<MultiMetric>() {
			public void run( MultiMetric metric ) {
				metrics.add( metric );
			}
		});
		return metrics;
	}

	private Report getReportByName( String name ) {
		Report report = reports.get( name );
		if( report == null ) {
			throw new RuntimeException( "Could not find report: " + name );
		}
		return report;
	}
	private MetricTree getTreeByName( String name ) {
		MetricTree tree = trees != null ? trees.get( name ) : null;
		if( tree != null ) {
			return tree;
		}
		throw new RuntimeException( "Could not find tree: '" + name + "'" );
	}
	
	private MetricTree loadTreeByName( String name ) {
		MetricTree tree = store.getMetricTree( name );
		if( tree != null ) {
			trees.put( tree.getName(), tree );
			return tree;
		}
		throw new RuntimeException( "Could not find tree: '" + name + "'" );
	}
	
	private MetricTreeNodeDto populateNodes( Map<String,MetricTreeNode> nodes, MetricTree tree, MetricTreeNode node, MetricTreeParentNodeDto ancestor ) {
		Collection<? extends MetricTreeNode> children = node.getChildren().values();
		if( children.size() > 0 ) {
			List<MetricTreeNodeDto> dchildren = new ArrayList<MetricTreeNodeDto>();
			MetricTreeParentNodeDto parent = new MetricTreeParentNodeDto( ancestor, tree.getName(), node.getId(), node.getName() );
			for( MetricTreeNode child : children ) {
				dchildren.add( populateNodes(nodes, tree, child, parent) );
			}
			Collections.sort( dchildren, new Comparator<MetricTreeNodeDto>() {
				public int compare( MetricTreeNodeDto n1, MetricTreeNodeDto n2 ) {
					return n1.getName().compareTo(n2.getName());
				}
			} );
			parent.setChildren( dchildren );
			nodes.put( parent.getId(), node );
			return parent;
		}
		else {
			MetricTreeNodeDto child = new MetricTreeNodeDto( ancestor, tree.getName(), node.getId(), node.getName() );
			nodes.put( child.getId(), node );
			return child;
		}
	}
}

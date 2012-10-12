package org.scriptbox.ui.server.tree;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.metrics.query.groovy.Report;
import org.scriptbox.metrics.query.groovy.ReportBuilder;
import org.scriptbox.metrics.query.groovy.ReportElement;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueries;
import org.scriptbox.ui.shared.tree.MetricChartDto;
import org.scriptbox.ui.shared.tree.MetricQueryDto;
import org.scriptbox.ui.shared.tree.MetricRangeDto;
import org.scriptbox.ui.shared.tree.MetricReportDto;
import org.scriptbox.ui.shared.tree.MetricReportSummaryDto;
import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;
import org.scriptbox.ui.shared.tree.ReportQueryDto;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

// @Service("treeService")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="treeService",scope=ServiceScope.SESSION)
public class MetricTreeGWTInterfaceImpl implements MetricTreeGWTInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricTreeGWTInterfaceImpl.class );
	
	private MetricStore store;
	private List<String> reportPaths;
	
	private List<MetricTree> trees;
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

	@Override
	public ArrayList<MetricTreeDto> getTrees() {
		store.begin();
		try {
			trees = store.getAllMetricTrees();
			ArrayList<MetricTreeDto> ret = new ArrayList<MetricTreeDto>();
			for( MetricTree tree : trees ) {
				ret.add( new MetricTreeDto(tree.getName()) );
			}
			return ret;
		}
		finally {
			store.end();
		}
	}

	public MetricTreeParentNodeDto getRoot( MetricTreeDto treeDto ) {
		store.begin();
		try {
			MetricTree tree = getTreeByName(treeDto.getTreeName());
			MetricTreeNode root = tree.getRoot();
			
			Map<String,MetricTreeNode> nodes = new HashMap<String,MetricTreeNode>();
			MetricTreeParentNodeDto ret = (MetricTreeParentNodeDto)populateNodes( nodes, tree, root );
			allNodes.put( tree, nodes );
			return ret;
		}
		finally {
			store.end();
		}
	}
	
	public MetricRangeDto getMetrics( MetricQueryDto query ) {
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
		MetricRange range = seq.getRange(dr.getStart().getTime(), dr.getEnd().getTime() );
		return new MetricRangeDto( 
			dr.getStart().getTime(), 
			dr.getEnd().getTime(), 
			range.getStart(), 
			range.getEnd(), 
			range.getMetrics(30) );
	}

	public ArrayList<MetricReportSummaryDto> getReports() {
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
						
						ArrayList<String> charts = new ArrayList<String>();
						for( ReportElement element : report.getElements() ) {
							charts.add( element.getTitle() );
						}
						MetricReportSummaryDto summaryDto = new MetricReportSummaryDto();
						summaryDto.setName( report.getName () );
						summaryDto.setCharts( charts );
						ret.add( summaryDto );
					}
					catch( Exception ex ) {
						LOGGER.error( "getReports: failed loading report: " + reportFile );
					}
				}
			}
			else {
				LOGGER.warn( "getReports: report directory does not exist: " + reportPath );
			}
		}
		return ret;
	}
	
	public MetricReportDto getReport( ReportQueryDto query ) throws Exception {
		MetricTree tree = getTreeByName( query.getTreeName() );
		Report report = getReportByName( query.getReportName() );
		
		MetricReportDto reportDto = new MetricReportDto();
		for( ReportElement element : report.getElements() ) {
			Map<? extends MetricProvider,? extends MetricRange> result = MetricQueries.query(
				store, tree, element.getExpression(), report.getStart(), report.getEnd(), report.getChunk());
			
			MetricChartDto chartDto = new MetricChartDto();
			for( Map.Entry<? extends MetricProvider, ? extends MetricRange> entry : result.entrySet() ) {
				MetricProvider provider = entry.getKey();
				MetricRange range =  entry.getValue();
				DateRange dr = provider.getFullDateRange();
				chartDto.addSeries( new MetricRangeDto( 
					dr.getStart().getTime(), 
					dr.getEnd().getTime(), 
					range.getStart(), 
					range.getEnd(), 
					range.getMetrics(30)) );
			}
			reportDto.addChart( chartDto );
		}
		return reportDto;
	}
	// public Metric whitelist1( Metric value ) { return null; }
	
	private Report getReportByName( String name ) {
		Report report = reports.get( name );
		if( report == null ) {
			throw new RuntimeException( "Could not find report: " + name );
		}
		return report;
	}
	private MetricTree getTreeByName( String name ) {
		for( MetricTree tree : trees ) {
			if( tree.getName().equals(name) ) {
				return tree;
			}
		}
		throw new RuntimeException( "Could not find tree: '" + name + "'" );
	}
	
	private MetricTreeNodeDto populateNodes( Map<String,MetricTreeNode> nodes, MetricTree tree, MetricTreeNode node ) {
		Collection<? extends MetricTreeNode> children = node.getChildren().values();
		if( children.size() > 0 ) {
			List<MetricTreeNodeDto> dchildren = new ArrayList<MetricTreeNodeDto>();
			MetricTreeParentNodeDto parent = new MetricTreeParentNodeDto( tree.getName(), node.getId(), node.getName() );
			for( MetricTreeNode child : children ) {
				dchildren.add( populateNodes(nodes, tree, child) );
			}
			parent.setChildren( dchildren );
			nodes.put( parent.getId(), node );
			return parent;
		}
		else {
			MetricTreeNodeDto child = new MetricTreeNodeDto( tree.getName(), node.getId(), node.getName() );
			nodes.put( child.getId(), node );
			return child;
		}
	}
}

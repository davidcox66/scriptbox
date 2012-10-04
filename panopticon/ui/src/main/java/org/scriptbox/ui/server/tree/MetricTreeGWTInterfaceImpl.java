package org.scriptbox.ui.server.tree;

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
import org.scriptbox.ui.shared.tree.MetricQueryDto;
import org.scriptbox.ui.shared.tree.MetricRangeDto;
import org.scriptbox.ui.shared.tree.MetricTreeDto;
import org.scriptbox.ui.shared.tree.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.tree.MetricTreeNodeDto;
import org.scriptbox.ui.shared.tree.MetricTreeParentNodeDto;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("treeService")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="treeService",scope=ServiceScope.SESSION)
public class MetricTreeGWTInterfaceImpl implements MetricTreeGWTInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricTreeGWTInterfaceImpl.class );
	
	@Autowired
	private MetricStore store;
	
	private List<MetricTree> trees;
	private Map<MetricTree,Map<String,MetricTreeNode>> allNodes = new HashMap<MetricTree,Map<String,MetricTreeNode>>();
	
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
		DateRange dr = seq.getDateRange();
		MetricRange range = seq.getRange(dr.getStart().getTime(), dr.getEnd().getTime(), 30 );
		return new MetricRangeDto( 
			dr.getStart().getTime(), 
			dr.getEnd().getTime(), 
			range.getStart(), 
			range.getEnd(), 
			range.getMetrics() );
	}

	// public Metric whitelist1( Metric value ) { return null; }
	
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

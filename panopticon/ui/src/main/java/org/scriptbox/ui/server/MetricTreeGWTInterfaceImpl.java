package org.scriptbox.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.ui.shared.MetricTreeGWTInterface;
import org.scriptbox.ui.shared.TreeDto;
import org.scriptbox.ui.shared.TreeNodeDto;
import org.scriptbox.ui.shared.TreeParentNodeDto;
import org.scriptbox.util.gwt.server.remote.ExportableService;
import org.scriptbox.util.gwt.server.remote.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("treeService")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@ExportableService(value="treeService",scope=ServiceScope.SESSION)
public class MetricTreeGWTInterfaceImpl implements MetricTreeGWTInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger( MetricTreeGWTInterfaceImpl.class );
	
	@Autowired
	private MetricStore store;
	
	private List<MetricTree> trees;
	private Map<MetricTree,Map<String,MetricTreeNode>> allNodes = new HashMap<MetricTree,Map<String,MetricTreeNode>>();
	
	@Override
	public ArrayList<TreeDto> getTrees() {
		store.begin();
		try {
			trees = store.getAllMetricTrees();
			ArrayList<TreeDto> ret = new ArrayList<TreeDto>();
			for( MetricTree tree : trees ) {
				ret.add( new TreeDto(tree.getName()) );
			}
			return ret;
		}
		finally {
			store.end();
		}
	}

	public TreeParentNodeDto getRoot( TreeDto treeDto ) {
		store.begin();
		try {
			MetricTree tree = getTreeByName(treeDto.getTreeName());
			MetricTreeNode root = tree.getRoot();
			
			Map<String,MetricTreeNode> nodes = new HashMap<String,MetricTreeNode>();
			TreeParentNodeDto ret = (TreeParentNodeDto)populateNodes( nodes, tree, root );
			allNodes.put( tree, nodes );
			return ret;
		}
		finally {
			store.end();
		}
	}
	
	private MetricTree getTreeByName( String name ) {
		for( MetricTree tree : trees ) {
			if( tree.getName().equals(name) ) {
				return tree;
			}
		}
		throw new RuntimeException( "Could not find tree: '" + name + "'" );
	}
	
	private TreeNodeDto populateNodes( Map<String,MetricTreeNode> nodes, MetricTree tree, MetricTreeNode node ) {
		Collection<? extends MetricTreeNode> children = node.getChildren().values();
		if( children.size() > 0 ) {
			List<TreeNodeDto> dchildren = new ArrayList<TreeNodeDto>();
			TreeParentNodeDto parent = new TreeParentNodeDto( tree.getName(), node.getId(), node.getName() );
			for( MetricTreeNode child : children ) {
				dchildren.add( populateNodes(nodes, tree, child) );
			}
			parent.setChildren( dchildren );
			nodes.put( parent.getId(), node );
			return parent;
		}
		else {
			TreeNodeDto child = new TreeNodeDto( tree.getName(), node.getId(), node.getName() );
			nodes.put( child.getId(), node );
			return child;
		}
	}
}

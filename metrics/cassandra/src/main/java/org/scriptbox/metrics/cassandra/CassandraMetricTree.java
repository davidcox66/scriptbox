package org.scriptbox.metrics.cassandra;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.SuperCfUpdater;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;

public class CassandraMetricTree extends MetricTree {

	public static final String ROOT_NODE_NAME = "Metrics";
	
	CassandraMetricStore store;
	CassandraMetricTreeNode root;
	
	public CassandraMetricTree( CassandraMetricStore store, String name, List<MetricResolution> resolutions ) {
		super( name, resolutions );
		this.store = store;
	}
	
	
	public MetricTreeNode getRoot() {
		if( root != null ) {
			return root;
		}
		
		class NodeInfo
		{
			CassandraMetricTreeNode node;
			String parentId;
			NodeInfo( CassandraMetricTreeNode node, String parentId ) {
				this.node = node;
				this.parentId = parentId;
			}
		};
		
		final Map<String,NodeInfo> allNodesById = new HashMap<String,NodeInfo>();
		SuperCfResult<String,String,String> result = store.metricTreeTemplate.querySuperColumns(name);
		if( result.hasResults() ) {
			Collection<String> ids = result.getSuperColumns();
			for( String id : ids ) {
				 result.applySuperColumn(id);
				 CassandraMetricTreeNode node = new CassandraMetricTreeNode( 
				     CassandraMetricTree.this,
				     id,
				     result.getString("name"),
				     result.getString("type") );
				 NodeInfo info = new NodeInfo( node, result.getString("parent") );
				 allNodesById.put( result.getActiveSuperColumn(), info );
			 }
		}  
		if( !allNodesById.isEmpty() ) {
			for( Map.Entry<String,NodeInfo> entry : allNodesById.entrySet() ) {
				String nodeId = entry.getKey();
				NodeInfo nodeInfo = entry.getValue();
				if( StringUtils.isEmpty(nodeInfo.parentId) ) {
					root = nodeInfo.node;
				}
				else {
					NodeInfo parentNodeInfo = allNodesById.get( nodeInfo.parentId );
					if( parentNodeInfo == null ) {
						throw new RuntimeException( "Metric tree corrupt - node '" + nodeId + "' refers to nonexistent parent '" + nodeInfo.parentId );
					}
					nodeInfo.node.parent = parentNodeInfo.node; 
				}
			}
			if( root == null ) {
				throw new RuntimeException( "Metric tree corrupt - no root node found" );
			}
			for( NodeInfo nodeInfo : allNodesById.values() ) {
				CassandraMetricTreeNode node = nodeInfo.node;
				if( node.parent != null ) {
					node.parent.children.put( node.getName(), node );
				}
			}
		}
		return root;
	}
	
	public void delete() {
		store.metricTreeTemplate.deleteColumn( CassandraMetricStore.ALL_NAMES_KEY, name );
	}
	
	void persist() {
		if( StringUtils.isEmpty(name) ) {
			throw new RuntimeException( "Tree name cannot be null");
		}
		SuperCfTemplate<String,String,String> tmpl = store.metricTreeTemplate;
		SuperCfUpdater<String,String,String> updater = tmpl.createUpdater(CassandraMetricStore.ALL_NAMES_KEY,name);
		updater.setByteBuffer("resolutions", ObjectSerializer.get().toByteBuffer(resolutions) );
		tmpl.update( updater );
		root = new CassandraMetricTreeNode(this, ROOT_NODE_NAME, ROOT_NODE_NAME, "root" );
		root.persist();
	}
}

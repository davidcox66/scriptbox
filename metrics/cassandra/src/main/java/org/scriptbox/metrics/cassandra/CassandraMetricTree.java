package org.scriptbox.metrics.cassandra;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfRowMapper;
import me.prettyprint.cassandra.service.template.SuperCfUpdater;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;

public class CassandraMetricTree extends MetricTree {

	CassandraMetricStore store;
	CassandraMetricTreeNode root;
	
	public CassandraMetricTree( CassandraMetricStore store, String name, List<MetricResolution> resolutions ) {
		super( name, resolutions );
		this.store = store;
	}
	
	public boolean isValidResolution( int resolution ) {
		return resolutions.contains( resolution );
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
		store.metricTreeTemplate.querySuperColumns(name, null, new SuperCfRowMapper<String, String, String, CassandraMetricTreeNode>() {
			 public CassandraMetricTreeNode mapRow(SuperCfResult<String, String, String> results) {
				 CassandraMetricTreeNode node = new CassandraMetricTreeNode( 
				     CassandraMetricTree.this,
				     results.getActiveSuperColumn(),
				     results.getString("name"),
				     results.getString("type") );
				 NodeInfo info = new NodeInfo( node, results.getString("parent") );
				 allNodesById.put( results.getActiveSuperColumn(), info );
				 return node;
			 }
		} ); 
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
		SuperCfUpdater<String,String,String> updater = store.metricTreeTemplate.createUpdater(CassandraMetricStore.ALL_NAMES_KEY,name);
		updater.setByteBuffer("resolutions", ObjectSerializer.get().toByteBuffer(resolutions) );
	}
}

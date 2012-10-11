package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.SuperCfUpdater;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.metrics.query.MetricQueryContext;

public class CassandraMetricTreeNode implements MetricTreeNode {

	private String name;
	private String type;
	private String id;
	
	CassandraMetricTree tree;
	CassandraMetricTreeNode parent;
	Map<String,CassandraMetricTreeNode> children = new HashMap<String,CassandraMetricTreeNode>();
	
	public CassandraMetricTreeNode( CassandraMetricTree tree, String id, String name, String type ) {
		this.tree = tree;
		this.name = name;
		this.type = type;
	}
	@Override
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	@Override
	public MetricTreeNode getParent() {
		return parent;
	}

	public int getNearestResolutionSeconds( int seconds ) {
		return tree.getNearestResolution(seconds).getSeconds();
	}
	
	public boolean isPersistent() {
		return true;
	}

	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return getMetricSequence().getMetrics( ctx );
	}
	
	public MetricTreeNode getChild( String name ) {
		return children.get( name );
	}
	
	public MetricTreeNode getChild( String name, String type ) {
		CassandraMetricTreeNode child = children.get( name );
		if( child == null ) {
			child = new CassandraMetricTreeNode( tree, null, name, type );
			child.parent = this;
			child.persist();
			children.put( name, child );
		}
		return child;
	}

	void persist() {
		String id = getId();
		if( StringUtils.isEmpty(id) ) {
			throw new RuntimeException( "ID cannot be null for node: '" + name + "'" );
		}
		SuperCfTemplate<String, String, String> tmpl = getStore().metricTreeTemplate;
		SuperCfUpdater<String,String,String> updater = tmpl.createUpdater( tree.getName(), id );
		updater.setString("name", name );
		updater.setString("parent", parent != null ? parent.getId() : "" );
		updater.setString("type", type );
		tmpl.update( updater );
	}
	
	@Override
	public Map<String,? extends MetricTreeNode> getChildren() {
		return children;
	}

	CassandraMetricStore getStore() {
		return tree.store;
	}
	
	public String getId() {
		if( id == null ) { 
			StringBuilder builder = new StringBuilder();
			builder.append( tree.getName() );
			List<String> names = new ArrayList<String>();
			for( CassandraMetricTreeNode node = this ; node != null ; node = node.parent ) {
				names.add( node.name );
			}
			while( !names.isEmpty() ) {
				builder.append( "," );
				builder.append( names.remove(names.size()-1) );
			}
			id = builder.toString();
		}
		return id;
	}

	public String getId( int seconds ) {
		MetricResolution resolution = tree.getNearestResolution( seconds );
		return getId() + "," + resolution.getSeconds();
	}
	
	public boolean isSequenceAvailable() {
		return children.size() == 0;
	}
	public MetricSequence getMetricSequence() {
		if( !isSequenceAvailable() ) {
			throw new RuntimeException( "MetricSequences only allowed on leaf nodes: '" + getId() + "'");
		}
		return new CassandraMetricSequence( this );
	}
	
	public String toString() {
		return "CassandraMetricTreeNode{ name=" + name + ", type=" + type + ", id=" + id + "}";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CassandraMetricTreeNode other = (CassandraMetricTreeNode) obj;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
}

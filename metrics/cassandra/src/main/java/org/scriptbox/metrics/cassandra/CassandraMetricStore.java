package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfRowMapper;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftSuperCfTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ComparatorType;

import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.util.cassandra.Cassandra;
import org.scriptbox.util.cassandra.ColumnFamilyDefinition;

public class CassandraMetricStore implements MetricStore {

	public static final String METRIC_TREE_CF = "MetricTree";
	public static final String METRIC_SEQUENCE_CF = "MetricSequence";
	public static final String ALL_NAMES_KEY = "AllMetrics";

	Cluster cluster;
	Keyspace keyspace;
	SuperCfTemplate<String,String,String> metricTreeTemplate;
	ColumnFamilyTemplate<String,Long> metricSequenceTemplate;
	private boolean initialized;

	public void with( Runnable closure ) {
		begin();
		try {
			closure.run();
		}
		finally {
			end();
		}
	}
	
	public CassandraMetricStore() {
	}
	
	public Keyspace getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}
	
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void begin() {
		initialize();
		CassandraMetricRange.begin();
	}
	
	public void end() {
		CassandraMetricRange.end();
	}

	public MetricTree createMetricTree( String name, List<MetricResolution> resolutions ) {
		CassandraMetricTree tree = new CassandraMetricTree( this, name, resolutions );
		tree.persist();
		return tree;
	}
	
	public List<MetricTree> getAllMetricTrees() {
		List<MetricTree> ret = new ArrayList<MetricTree>();
		SuperCfResult<String,String,String> result = metricTreeTemplate.querySuperColumns(ALL_NAMES_KEY);
		if( result.hasResults() ) {
			Collection<String> names = result.getSuperColumns();
			for( String name : names ) {
				List<MetricResolution> resolutions = (List<MetricResolution>)ObjectSerializer.get().fromByteBuffer(result.getByteBuffer(name, "resolutions"));
				MetricTree tree = new CassandraMetricTree( this, name, resolutions );
				ret.add( tree );
			}
		}
		return ret;
	}
	
	public MetricTree getMetricTree( String name ) {
		List<String> names = new ArrayList<String>(1);
		names.add( name );
		List<MetricTree> trees = metricTreeTemplate.querySuperColumns(ALL_NAMES_KEY, names, mapper );
		return trees.size() > 0 ? trees.get(0) : null;
	}
	
	private void initialize() {
		if( !initialized ) {
			if( keyspace == null ) {
				throw new IllegalArgumentException( "Keyspace cannot be null");
			}
			if( cluster == null ) {
				throw new IllegalArgumentException( "Cluster cannot be null");
			}
			createSchemaIfNeeded();
			StringSerializer ser = StringSerializer.get();
			metricTreeTemplate = new ThriftSuperCfTemplate<String,String,String>( keyspace, METRIC_TREE_CF, ser, ser, ser );
			metricSequenceTemplate = new ThriftColumnFamilyTemplate<String,Long>( keyspace, METRIC_SEQUENCE_CF, ser, LongSerializer.get() );
			initialized = true;
		}
	}

	private void createSchemaIfNeeded() {
		Cassandra.createKeyspaceIfNeeded( cluster, keyspace.getKeyspaceName() );

		if( !Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), METRIC_TREE_CF) ) {
			ColumnFamilyDefinition tree = new ColumnFamilyDefinition();
			tree.setColumnFamilyName(CassandraMetricStore.METRIC_TREE_CF);
			tree.setComparator(ComparatorType.UTF8TYPE);
			tree.setSubComparator(ComparatorType.UTF8TYPE);
			tree.create( cluster, keyspace.getKeyspaceName() );
		}
			
		if( !Cassandra.isExistingColumnFamily(cluster, keyspace.getKeyspaceName(), METRIC_SEQUENCE_CF) ) {
			ColumnFamilyDefinition sequence = new ColumnFamilyDefinition();
			sequence.setColumnFamilyName(CassandraMetricStore.METRIC_SEQUENCE_CF);
			sequence.setComparator(ComparatorType.LONGTYPE);
			sequence.create( cluster, keyspace.getKeyspaceName() );
		}
		
	}
	private SuperCfRowMapper<String, String, String, List<MetricTree>> mapper = new SuperCfRowMapper<String, String, String, List<MetricTree>>() {
		 public List<MetricTree> mapRow(SuperCfResult<String, String, String> result) {
			List<MetricTree> trees = new ArrayList<MetricTree>();
			if( result.hasResults() )  {
				Collection<String> names = result.getSuperColumns();
				for( String treeName : names ) {
					List<MetricResolution> resolutions = (List<MetricResolution>)ObjectSerializer.get().fromByteBuffer(result.getByteBuffer(treeName, "resolutions"));
					trees.add( new CassandraMetricTree( CassandraMetricStore.this, treeName, resolutions) );
				}
			}
			return trees;
		 }
	};
}

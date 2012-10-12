package org.scriptbox.metrics.cassandra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.prettyprint.cassandra.model.HSlicePredicate;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.ObjectSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyRowMapper;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.MappedColumnFamilyResult;
import me.prettyprint.cassandra.service.template.SuperCfResult;
import me.prettyprint.cassandra.service.template.SuperCfRowMapper;
import me.prettyprint.cassandra.service.template.SuperCfTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftSuperCfTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ComparatorType;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricCache;
import org.scriptbox.metrics.model.SequenceCache;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.query.main.MetricProvider;
import org.scriptbox.metrics.query.main.MetricQueryContext;
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
		MetricCache.begin();
		SequenceCache.begin();
	}
	
	public void end() {
		MetricCache.end();
		SequenceCache.end();
	}

	public Map<? extends MetricProvider,? extends MetricRange> getAllMetrics( final Collection<? extends MetricProvider> nodes, final MetricQueryContext ctx ) {
	
		final long start = ctx.getStart();
		final long end = ctx.getEnd();
		final int seconds = ctx.getChunk();
		
	    List<String> toQueryIds = new ArrayList<String>();
	    Map<MetricProvider,MetricRange> ret = new HashMap<MetricProvider,MetricRange>();
	    final Map<String,Integer> resolutionsByNodeId = new HashMap<String,Integer>();
	    final Map<String,MetricRange> rangesById = new HashMap<String,MetricRange>();
	     
	    for( MetricProvider node : nodes ) {
	    	// CassandraMetricTreeNode cnode = (CassandraMetricTreeNode)node;
	    	MetricRange range = node.getMetrics( ctx );
			if( node.isPersistent() ) {
				CassandraMetricProvider cnode = (CassandraMetricProvider)node;
				int resolution = cnode.getNearestResolutionSeconds( seconds );
				if( !MetricCache.contains(range, resolution)) {
					String compositeId = cnode.getId( resolution );
					toQueryIds.add( compositeId );
					resolutionsByNodeId.put(compositeId, resolution);
					rangesById.put(compositeId, range);
				}
			}
	    	ret.put(node, range);
	    }
	    
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(start, end, false, Integer.MAX_VALUE );
		MappedColumnFamilyResult<String,Long,List<Metric>> result = metricSequenceTemplate.queryColumns(toQueryIds, predicate, 
			new ColumnFamilyRowMapper<String,Long,List<Metric>>() {
				public List<Metric> mapRow(ColumnFamilyResult<String, Long> results) {
					Integer resolution = resolutionsByNodeId.get( results.getKey() );
					return loadMetrics( start, end, resolution, results );
				}
		} );
		
		while( result.hasNext() ) {
			result.next();
			String compositeId = result.getKey();
			List<Metric> metrics = result.getRow();
			MetricCache.put( rangesById.get(compositeId), seconds, metrics );
		}
	    
	    return ret;	
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

	List<Metric> loadMetrics( long start, long end, int resolution, ColumnFamilyResult<String, Long> results) {
		Collection<Long> columns = results.getColumnNames();
		List<Metric> metrics = new ArrayList<Metric>(columns.size()+2); // 2 for the potential of adding an earlier and later value
	
		Iterator<Long> iter = columns.iterator();
		
		Long millis = null;
		Metric metric = null;
		if( iter.hasNext() ) {
			millis = iter.next();
			metric = new Metric( millis, results.getFloat(millis) );
			// If first metric is not near the beginning, try to find last previous value
			if( millis >= (start + resolution*1000) ) {
				Metric before = findRangeExteriorMetric(results.getKey(), start, null, true );
				metrics.add( new Metric(start, before != null ? before.getValue() : metric.getValue()) );
			}
			metrics.add( metric );
		}
		
		while( iter.hasNext() ) {
			millis = iter.next();
			metrics.add( new Metric(millis, results.getFloat(millis)) );
		}

		if( metrics.size() > 0 ) {
			metric = metrics.get( metrics.size()-1 );
			// If last value was not near the end, search beyond for the next value
			if( metric.getMillis() <= (end - resolution*1000) ) {
				Metric after = findRangeExteriorMetric(results.getKey(), end, Long.MAX_VALUE, false );
				metrics.add( new Metric(end, after != null ? after.getValue() : metric.getValue()) );
			}
		}
		return metrics;
	}
		
	Metric findRangeExteriorMetric( String compositeId, Long first, Long last, boolean reversed ) {
		HSlicePredicate<Long> predicate = new HSlicePredicate<Long>( LongSerializer.get() );
		predicate.setRange(first, last, reversed, 1 );
		ColumnFamilyResult<String,Long> result = metricSequenceTemplate.queryColumns( compositeId, predicate );
		Collection<Long> times = result.getColumnNames();
		if( times.size() > 0 ) {
			Long millis = times.iterator().next();
			return new Metric( millis, result.getFloat(millis) );
		}
		return null;
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

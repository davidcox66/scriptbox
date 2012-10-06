package org.scriptbox.metrics.cassandra;

import java.util.List;

import me.prettyprint.cassandra.service.tx.HTransactionTemplate;
import me.prettyprint.hector.api.ddl.ComparatorType;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.util.cassandra.Cassandra;
import org.scriptbox.util.cassandra.ColumnFamilyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraStoreTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( CassandraStoreTest.class );
	
	private static final String KEYSPACE = "Metrics";
	
	@BeforeClass
	public static void initializeKeyspace() throws Throwable {
		try {
			LOGGER.debug( "Initialing test" );
			Cassandra.start();
			Cassandra.setEmbeddedEnabled( true );
			Cassandra.initializeKeyspace("Metrics");
			
			ColumnFamilyDefinition tree = new ColumnFamilyDefinition();
			tree.setColumnFamilyName(CassandraMetricStore.METRIC_TREE_CF);
			tree.setComparator(ComparatorType.UTF8TYPE);
			tree.setSubComparator(ComparatorType.UTF8TYPE);
			tree.create( KEYSPACE );
			
			ColumnFamilyDefinition sequence = new ColumnFamilyDefinition();
			sequence.setColumnFamilyName(CassandraMetricStore.METRIC_SEQUENCE_CF);
			sequence.setComparator(ComparatorType.LONGTYPE);
			sequence.create( KEYSPACE );
			LOGGER.debug( "Finished initialization" );
		}
		catch( Throwable ex ) {
			LOGGER.error( "Error initializing test", ex );
			throw ex;
		}
	}
	
	@Test
	public void testLoadAndRetrieve() {
		final CassandraMetricStore store = new CassandraMetricStore();
		store.setKeyspace(Cassandra.getKeyspace(KEYSPACE));
		store.setCluster(Cassandra.getOrCreateCluster());
		
		final long now = System.currentTimeMillis();
		
		store.begin();
		HTransactionTemplate.execute( new Runnable() {
			public void run() {
				MetricTree tree = store.createMetricTree("Foo", MetricResolution.create(30,300,900) );
				MetricTreeNode root = tree.getRoot();
				MetricTreeNode source = root.getChild( "Source", "source" );
				MetricTreeNode attribute = source.getChild( "Attribute", "attribute" );
				MetricTreeNode metric = attribute.getChild( "Metric", "metric" );
				MetricSequence seq = metric.getMetricSequence();
				long millis = now;
				seq.record( new Metric(millis,1.0F) );
				millis += 60*1000;
				seq.record( new Metric(millis,2.0F) );
				millis += 60*1000;
				seq.record( new Metric(millis,3.0F) );
			}
		} );
		
		MetricTree tree = store.getMetricTree( "Foo" );
		Assert.assertNotNull( "Tree not found", tree );
		
		MetricTreeNode root = tree.getRoot();
		Assert.assertNotNull( "Root is null", root );
		
		MetricTreeNode source = root.getChild( "Source" );
		Assert.assertNotNull( "Source is null", source );
		
		MetricTreeNode attribute = source.getChild( "Attribute" );
		Assert.assertNotNull( "Attribute is null", attribute );
		
		MetricTreeNode metric = attribute.getChild( "Metric" );
		Assert.assertNotNull( "Metric is null", metric );
		
		MetricSequence seq = metric.getMetricSequence();
		Assert.assertNotNull( "MetricSequence is null", seq );
		
		MetricRange range = seq.getRange( now, now+120*1000, 30 );
		Assert.assertNotNull( "MetricRange is null", range );
		
		List<Metric> metrics = range.getMetrics();
		Assert.assertNotNull( "No metrics found", metrics );
		Assert.assertEquals( "Incorrect number of metrics", 3, metrics.size() );
		
		long millis = now;
		Metric mv = metrics.get(0);
		Assert.assertEquals( 1.0F, mv.getValue(), 0 );
		Assert.assertEquals( mv.getMillis(), millis );
		millis += 60*1000;
		
		mv = metrics.get(1);
		Assert.assertEquals( 2.0F, mv.getValue(), 0 );
		Assert.assertEquals( mv.getMillis(), millis );
		millis += 60*1000;
		
		mv = metrics.get(2);
		Assert.assertEquals( 3.0F, mv.getValue(), 0 );
		Assert.assertEquals( mv.getMillis(), millis );
	}
}

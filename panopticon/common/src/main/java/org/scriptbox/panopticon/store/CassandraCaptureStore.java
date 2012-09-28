package org.scriptbox.panopticon.store;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.events.BoxInvocationContext;
import org.scriptbox.box.plugins.jmx.capture.CaptureResult;
import org.scriptbox.box.plugins.jmx.capture.CaptureStore;
import org.scriptbox.box.plugins.quartz.QuartzInvocationContext;
import org.scriptbox.box.plugins.quartz.QuartzListener;
import org.scriptbox.metrics.cassandra.CassandraMetricStore;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.util.cassandra.CassandraDownTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import me.prettyprint.cassandra.service.tx.HTransactionTemplate;

public class CassandraCaptureStore implements BoxContextListener, CaptureStore, QuartzListener, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( CassandraCaptureStore.class );
	
	private Keyspace keyspace;
	private Cluster cluster;
	private String instance;
	private CassandraMetricStore cstore;

	private CassandraDownTemplate down = new CassandraDownTemplate();
	
	public void contextCreated( BoxContext context ) throws Exception {
		context.getBeans().put( "store", this );
	}
	
	public void contextShutdown( BoxContext context ) throws Exception {
		
	}
	public void executingScript( BoxInvocationContext invocation ) throws Exception {
		invocation.next();
	}

    public void jobStarted( final QuartzInvocationContext context ) {
        LOGGER.debug( "jobStarted: starting transaction" );
		down.invoke( new Runnable() {
			public void run() {
		        cstore.begin();
		        try {
		            HTransactionTemplate.execute( new Runnable() {
		                public void run() {
		                    try {
		                        context.next();
		                    }
		                    catch( Exception ex ) {
		                        throw new RuntimeException( "Error invoking job: " + context.getDetail(), ex );
		                    }
		                }
		            });
		        }
		        finally {
		            cstore.end();
		        }
			}
		} );
    }

	@Override
	public void store( final CaptureResult result ) throws Exception {
		down.invoke( new Runnable() {
			public void run() {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "store: result=" + result ); }
				if( result.value != null && result.value instanceof Number ) {
					float value = ((Number)result.value).floatValue();
					BoxContext ctx = BoxContext.getCurrentContext();
					Lookup beans = ctx.getBeans();
					MetricTree tree = beans.get( "metric.tree", MetricTree.class );
					if( tree == null ) {
						tree = cstore.createMetricTree(ctx.getName(), MetricResolution.create(30) );
						beans.put( "metric.tree", tree );
					}
					MetricTreeNode root = tree.getRoot();
					MetricTreeNode src  = root.getChild( result.process.getName(), "source" );
					MetricTreeNode inst = src.getChild(instance, "instance" ) ;
					MetricTreeNode attr = inst.getChild(result.attribute, "attribute" );
					MetricTreeNode stat = attr.getChild(result.statistic, "metric" );
					MetricSequence seq  = stat.getMetricSequence();
					seq.record( new Metric(result.millis,value) );
					
				}
			}
		} );
	}
	
	
	public Keyspace getKeyspace() {
		return keyspace;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void setKeyspace(Keyspace keyspace) {
		this.keyspace = keyspace;
	}

	public String getInstance() {
		return instance;
	}


	public void setInstance(String instance) {
		this.instance = instance;
	}


	public void afterPropertiesSet() throws Exception {
		if( cluster == null ) {
			throw new IllegalArgumentException( "Cluster cannot be null");
		}
		if( keyspace == null ) {
			throw new IllegalArgumentException( "Keyspace cannot be null");
		}
		cstore = new CassandraMetricStore();
		cstore.setCluster( cluster );
		cstore.setKeyspace( keyspace );
	}
}

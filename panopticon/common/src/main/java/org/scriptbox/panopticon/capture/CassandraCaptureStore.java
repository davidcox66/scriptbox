package org.scriptbox.panopticon.capture;

import me.prettyprint.cassandra.service.tx.HTransactionManager;
import me.prettyprint.cassandra.service.tx.HTransactionTemplate;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.events.BoxContextListener;
import org.scriptbox.box.events.BoxInvocationContext;
import org.scriptbox.box.plugins.quartz.QuartzInvocationContext;
import org.scriptbox.box.plugins.quartz.QuartzListener;
import org.scriptbox.metrics.cassandra.CassandraMetricStore;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.panopticon.jmx.JmxTypeVisitor;
import org.scriptbox.panopticon.jmx.JmxTypeWalker;
import org.scriptbox.util.cassandra.CassandraDownTemplate;
import org.scriptbox.util.common.obj.RunnableWithThrowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class CassandraCaptureStore implements BoxContextListener, CaptureStore, QuartzListener, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger( CassandraCaptureStore.class );
	
	private Keyspace keyspace;
	private Cluster cluster;
	private String instance;
	private CassandraMetricStore cstore;
	private NameSplitter nameSplitter;
	
	private CassandraDownTemplate down = new CassandraDownTemplate();
	
	public NameSplitter getNameSplitter() {
		return nameSplitter;
	}

	public void setNameSplitter(NameSplitter nameSplitter) {
		this.nameSplitter = nameSplitter;
	}

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
		down.invoke( new RunnableWithThrowable() {
			public void run() throws Throwable {
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
		down.invoke( new RunnableWithThrowable() {
			public void run() throws Throwable {
				if( LOGGER.isDebugEnabled() ) { LOGGER.debug( "store: result=" + result ); }
				if( result.value != null && !(result.value instanceof String) ) {
					BoxContext ctx = BoxContext.getCurrentContext();
					Lookup beans = ctx.getBeans();
					MetricTree tree = beans.get( "metric.tree", MetricTree.class );
					if( tree == null ) {
						tree = cstore.createMetricTree(ctx.getName(), MetricResolution.create(30,300,900,1800) );
						beans.put( "metric.tree", tree );
					}
					if( result.value instanceof Number ) {
						store( getAttributeNode(tree,result), result.millis, result.statistic, ((Number)result.value).floatValue() );
					}
					else {
						store( tree, result );
					}
				}
			}
		} );
	}

	public void flush() throws Exception {
		HTransactionManager.getInstance().flushAll();
	}
	
	private void store( final MetricTree tree, final CaptureResult result ) {
		JmxTypeWalker walker = new JmxTypeWalker( new JmxTypeVisitor() {
			
			private MetricTreeNode attr;
			
			private MetricTreeNode attr() {
				// cache the value as it may get called multiple times for the same structure
				if( attr == null ) {
					attr = getAttributeNode( tree, result );
				}
				return attr;
			}
		
			public void value( String prefix, String name, Object obj ) {
				if( obj != null && obj instanceof Number ) {
					store( attr(), result.millis, name, ((Number)obj).floatValue() );
				}
			}
			public void enter( String prefix, String name, Object object ) {
				
			}
			public void exit( String prefix, String name, Object object ) {
				
			}
		} );
		walker.walk("", result.statistic, result.value );
	}

	private void store( final MetricTreeNode attr, long millis, String name, float value ) {
		MetricTreeNode stat = attr.getChild(name, "metric" );
		MetricSequence seq  = stat.getMetricSequence();
		seq.record( new Metric(millis,value) );
	}
	
	private MetricTreeNode getAttributeNode( final MetricTree tree, final CaptureResult result ) {
		MetricTreeNode root = tree.getRoot();
		MetricTreeNode src  = root.getChild( result.process.getName(), "source" );
		MetricTreeNode inst = src.getChild(instance, "instance" ) ;
		MetricTreeNode attr = inst;
		//
		// Optional break up which typically is delimited ObjectName into a series of children
		// instead of a single long name
		//
		if( nameSplitter != null ) {
			String[] names = nameSplitter.split( result.attribute );
			for( String name : names ) {
				attr = attr.getChild(name, "attribute" );
			}
		}
		else {
			attr = inst.getChild(result.attribute, "attribute" );
		}
		return attr;
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

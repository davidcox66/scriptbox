package org.scriptbox.panopticon.store;

import me.prettyprint.hector.api.Keyspace;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.container.Lookup;
import org.scriptbox.box.plugins.jmx.capture.CaptureResult;
import org.scriptbox.box.plugins.jmx.capture.CaptureStore;
import org.scriptbox.metrics.cassandra.CassandraMetricStore;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricResolution;
import org.scriptbox.metrics.model.MetricSequence;
import org.scriptbox.metrics.model.MetricTree;
import org.scriptbox.metrics.model.MetricTreeNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class CassandraCaptureStore implements CaptureStore, InitializingBean {

	@Autowired
	private Keyspace keyspace;
	
	private String instance;
	private CassandraMetricStore cstore;
	
	@Override
	public void store(CaptureResult result) throws Exception {
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
	
	public String getInstance() {
		return instance;
	}


	public void setInstance(String instance) {
		this.instance = instance;
	}


	public void afterPropertiesSet() throws Exception {
		cstore = new CassandraMetricStore();
		cstore.setKeyspace( keyspace );
		cstore.initialize();
	}
}

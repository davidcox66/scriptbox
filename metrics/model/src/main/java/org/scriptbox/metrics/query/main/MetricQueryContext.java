package org.scriptbox.metrics.query.main;

import org.scriptbox.metrics.model.MetricStore;
import org.scriptbox.metrics.model.MetricTree;

public class MetricQueryContext {

	private MetricStore store;
	private MetricTree tree;
	private long start;
	private long end;
	private int chunk;
	
	public MetricQueryContext( MetricStore store, MetricTree tree, long start, long end, int chunk ) {
		this.store = store;
		this.tree = tree;
		this.start = start;
		this.end = end;
		this.chunk = chunk;
	}

	public MetricStore getStore() {
		return store;
	}
	public MetricTree getTree() {
		return tree;
	}
	public long getStart() {
		return start;
	}
	public long getEnd() {
		return end;
	}
	public int getChunk() {
		return chunk;
	}
	
}

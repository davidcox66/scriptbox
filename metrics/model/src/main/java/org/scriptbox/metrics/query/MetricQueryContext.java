package org.scriptbox.metrics.query;

import org.scriptbox.metrics.model.MetricTree;

public class MetricQueryContext {

	private MetricTree tree;
	private long start;
	private long end;
	private int chunk;
	
	public MetricQueryContext( MetricTree tree, long start, long end, int chunk ) {
		this.tree = tree;
		this.start = start;
		this.end = end;
		this.chunk = chunk;
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

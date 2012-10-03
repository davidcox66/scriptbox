package org.scriptbox.ui.shared.tree;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.scriptbox.metrics.model.Metric;
import org.scriptbox.ui.shared.timed.TimeBasedLoadResult;

public class MetricRangeDto implements TimeBasedLoadResult<Metric>, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Date first;
	private Date last;
	private Date start;
	private Date end;
	private float min;
	private float max;
	private List<Metric> metrics;
	private boolean changes;
	
	public MetricRangeDto() {
	}
	
	public MetricRangeDto( long first, long last, long start, long end, float min, float max, List<Metric> metrics ) {
		this.first = new Date(first);
		this.last = new Date(last);
		this.start = new Date(start);
		this.end = new Date(end);
		this.min = min;
		this.max = max;
		this.metrics = metrics;
	
		// Like to know if the metrics are flat-lined. This has some bearing on GXT charts
		// which have a problem in the browser compiled version with a flat chart
		if( metrics.size() > 0 ) {
			float initial = metrics.get(0).getValue();
			for( Metric m : metrics ) {
				if( m.getValue() != initial ) {
					changes = true;
					break;
				}
			}
		}
	}

	public float getMin() {
		return min < 0 ? 0 : min;
	}

	public float getMax() {
		return max < 0 ? 0 : max;
	}

	public boolean isChanges() {
		return changes;
	}
	
	public Date getStart() {
		return start;
	}

	public Date getEnd() {
		return end;
	}

	public Date getFirst() {
		return first;
	}
	
	public Date getLast() {
		return last;
	}
	
	public List<Metric> getData() {
		return metrics;
	}
	
}

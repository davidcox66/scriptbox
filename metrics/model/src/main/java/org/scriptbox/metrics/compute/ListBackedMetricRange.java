package org.scriptbox.metrics.compute;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.scriptbox.metrics.model.DateRange;
import org.scriptbox.metrics.model.Metric;
import org.scriptbox.metrics.model.MetricRange;
import org.scriptbox.metrics.query.main.MetricQueryContext;

public class ListBackedMetricRange extends MetricRange {

	private String name;
	private String id;
	private List<Metric> metrics;
	private DateRange fullDateRange;
	
	public ListBackedMetricRange( String name, String id, DateRange fullDateRange, long start, long end, List<Metric> metrics ) {
		super( start, end );
		this.name = name;
		this.id = id;
		this.metrics = metrics;
		this.fullDateRange = fullDateRange;
	}

	public void setStart( long start ) {
		this.start = start;
	}
	
	public void setEnd( long end ) {
		this.end = end;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isPersistent() {
		return false;
	}

	@Override
	public List<Metric> getMetrics(int resolution) {
		return metrics;
	}

	public MetricRange getMetrics( MetricQueryContext ctx ) {
		return this;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
	
	public void setFullDateRange(DateRange fullDateRange) {
		this.fullDateRange = fullDateRange;
	}

	public DateRange getFullDateRange() {
		return fullDateRange;
	}

	@Override
	public Iterator<Metric> getIterator(int resolution) {
		return metrics.iterator();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((metrics == null) ? 0 : metrics.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListBackedMetricRange other = (ListBackedMetricRange) obj;
		if (metrics == null) {
			if (other.metrics != null)
				return false;
		} else if (!metrics.equals(other.metrics))
			return false;
		return true;
	}

	public String toString() {
		return "ListBackedMetricRange{ name=" + getName() + ", start=" + new Date(getStart()) + ", end=" + new Date(getEnd()) + ", size=" + metrics.size() + " }";
	}
	
}

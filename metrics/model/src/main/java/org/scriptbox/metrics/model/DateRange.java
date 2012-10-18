package org.scriptbox.metrics.model;

import java.util.Date;

/**
 * A simple data object to retain the start and end of a date range. This is used mostly for
 * holding the date range of the full sequence of metrics.
 * 
 * @author david
 */
public class DateRange {

	private Date start;
	private Date end;
	
	public DateRange() {
	}
	
	public DateRange( Date start, Date end ) {
		this.start = start;
		this.end = end;
	}
	
	public Date getStart() {
		return start;
	}
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	
	
}
